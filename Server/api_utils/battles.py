import pymongo
from pymongo.errors import PyMongoError

import uuid

from utils import send_multi_message

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

WAITING_ROOM = 'waiting_room'
BATTLES = 'battles'


def join_waiting_room(uid):
    db[WAITING_ROOM].insert_one({"_id": uid})


pipeline = [{'$match': {'operationType': 'insert'}}]
stream = db["users"].watch(pipeline)


def start_matchmaking():
    print('starting watching collection')

    try:
        for _ in stream:

            if db[WAITING_ROOM].count() >= 2:
                users = list(db[WAITING_ROOM].aggregate([{"$sample": {"size": 2}}]))
                print(users)

                uids = [user['_id'] for user in users]

                db[WAITING_ROOM].delete_many({"_id": {"$in": users}})
                players = [{"uid": user['uid'], "score": -1} for user in users]

                battle = {
                    "_id": uuid.uuid4(),
                    "players": players
                }
                db[BATTLES].insert_one(battle)

                send_multi_message(battle, uids)

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)


def stop_matchmaking():
    global stream
    stream.close()
