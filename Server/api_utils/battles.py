import pymongo
from pymongo.errors import PyMongoError

import simplejson
import time
import uuid

from api_utils import users, questions

from utils import send_multi_message

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

WAITING_ROOM = 'waiting_room'
BATTLES = 'battles'

# db[WAITING_ROOM] should not be empty

pipeline = [{"$match": {"operationType": {"$in": ['insert', 'delete']}}}]
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


def leave_waiting_room(uid):
    resp = {}

    count = db[WAITING_ROOM].delete_one({"_id": uid}).deleted_count

    if count != 1:
        resp['success'] = False
        resp['message'] = 'Could not leave waiting room'
    else:
        resp['success'] = True
    return resp


def start_matchmaking():
    print('starting watching collection')

    try:
        for _ in stream:

            print('users in waiting room ', db[WAITING_ROOM].count())
            if db[WAITING_ROOM].count() >= 3:
                random_users = list(db[WAITING_ROOM].aggregate([
                    {"$match": {"dummy_key": {"$exists": False}}},
                    {"$sample": {"size": 2}}
                ]))
                print(random_users)

                uids = [user['_id'] for user in random_users]

                db[WAITING_ROOM].delete_many({"_id": {"$in": random_users}})
                players = [{"uid": user['_id'], "score": 0} for user in random_users]

                battle = {
                    "_id": str(uuid.uuid4()),
                    "players": players
                }

                db[BATTLES].insert_one(battle)

                tokens = users.get_fcm_tokens(uids)
                battle['players'] = simplejson.dumps(players)

                db[WAITING_ROOM].remove({"_id": {"$in": uids}})

                random_questions = questions.get_random_questions(10)
                db[BATTLES].update_one(
                    {"_id": battle['_id']},
                    {"$set": {"questions": random_questions}}
                )
                send_multi_message(battle, tokens)

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)


def get_battle_questions(bid):
    resp = {}

    battle_que = db[BATTLES].find_one(
        {"_id": bid},
        {"_id": 0, "questions": 1}
    )['questions']

    if battle_que is None:
        resp['success'] = False
        resp['message'] = 'Could not get questions'
    else:
        resp['success'] = True
        resp['questions'] = battle_que

    return resp


def stop_matchmaking():
    global stream
    stream.close()
