import uuid
from datetime import datetime, timedelta

import pymongo
import simplejson
from pymongo import ReturnDocument
from pymongo.errors import PyMongoError

from api_utils import users, questions
from api_utils.scheduling import scheduler
from utils import send_multi_message


DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

WAITING_ROOM = 'waiting_room'
BATTLES = 'battles'

# db[WAITING_ROOM] should not be empty

pipeline = [{"$match": {"operationType": {"$in": ['insert', 'delete']}}}]
wait_stream = db[WAITING_ROOM].watch(pipeline)

pipeline = [{"$match": {"operationType": {"$in": ['update']}}}]
battle_stream = db[BATTLES].watch(pipeline)


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
    print('starting watching matchmaking')

    try:
        for _ in wait_stream:

            print('users in waiting room ', db[WAITING_ROOM].count())
            if db[WAITING_ROOM].count() >= 3:
                random_users = list(db[WAITING_ROOM].aggregate([
                    {"$match": {"dummy_key": {"$exists": False}}},
                    {"$sample": {"size": 2}}
                ]))
                print(random_users)

                uids = [user['_id'] for user in random_users]

                db[WAITING_ROOM].delete_many({"_id": {"$in": random_users}})
                players = [{"uid": user['_id'], "score": -1} for user in random_users]

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

                data = {
                    "type": "matchmaking",
                    "payload": simplejson.dumps(battle)
                }
                send_multi_message(data, tokens)

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
    global wait_stream
    wait_stream.close()


def update_battle_score(bid, uid, score):
    resp = {}

    try:
        battle = db[BATTLES].find_one_and_update(
            {"_id": bid, "players.uid": uid},
            {"$set": {"players.$.score": score}},
            return_document=ReturnDocument.AFTER
        )

        updated = len(list(filter(lambda player: player['score'] != -1, battle['players'])))

        if updated == 1:
            wait_for_score_updates(bid)

        resp['success'] = True
    except PyMongoError as e:
        resp['success'] = False
        resp['message'] = str(e)
    finally:
        return resp


def wait_for_score_updates(bid):
    scheduler.add_job(func=send_score_updates, args=[bid], trigger="date", id=bid,
                      run_date=datetime.now() + timedelta(seconds=15))


def send_score_updates(bid):
    players = db[BATTLES].find_one_and_update(
        {"_id": bid},
        {"$set": {"status": "finished"}},
        {"_id": 0, "players": 1}
    )["players"]

    uids = [player['uid'] for player in players]
    tokens = users.get_fcm_tokens(uids)

    data = {
        "type": "score-update",
        "payload": simplejson.dumps(players)
    }
    send_multi_message(data, tokens)


def watch_battles():
    print('starting watching battle scores')

    try:
        for change in battle_stream:
            battle = change['fullDocument']

            remaining = len(list(filter(lambda player: player['score'] == -1, battle['players'])))
            if remaining == 0:
                send_score_updates(battle['_id'])
                scheduler.remove_job(job_id=battle['_id'])

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)
