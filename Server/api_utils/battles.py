import pymongo
from pymongo.errors import PyMongoError

import simplejson
import uuid

from api_utils.users import get_fcm_tokens
from utils import send_multi_message

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

WAITING_ROOM = 'waiting_room'
BATTLES = 'battles'

# db[WAITING_ROOM] should not be empty

pipeline = [{'$match': {'operationType': 'insert'}}]
stream = db[WAITING_ROOM].watch(pipeline)


def join_waiting_room(uid):
    resp = {}

    room = db[WAITING_ROOM].insert_one({"_id": uid})

    if room is None:
        resp['success'] = False
        resp['message'] = 'Could not join waiting room'
    else:
        resp['success'] = True
    return resp


def start_matchmaking():
    print('starting watching collection')

    try:
        for _ in stream:

            print('users in waiting room ', db[WAITING_ROOM].count())
            if db[WAITING_ROOM].count() >= 3:
                users = list(db[WAITING_ROOM].aggregate([
                    {"$match": {"dummy_key": {"$exists": False}}},
                    {"$sample": {"size": 2}}
                ]))
                print(users)

                uids = [user['_id'] for user in users]

                db[WAITING_ROOM].delete_many({"_id": {"$in": users}})
                players = [{"uid": user['_id'], "score": -1} for user in users]

                battle = {
                    "_id": str(uuid.uuid4()),
                    "players": players
                }

                db[BATTLES].insert_one(battle)

                tokens = get_fcm_tokens(uids)
                print('tokens = ', tokens)

                battle['players'] = simplejson.dumps(players)

                print(battle)

                send_multi_message(battle, tokens)

                db[WAITING_ROOM].remove({"_id": {"$in": uids}})

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)


def stop_matchmaking():
    global stream
    stream.close()
