import uuid
from datetime import datetime, timedelta

import simplejson
from pymongo import ReturnDocument, MongoClient
from pymongo.errors import PyMongoError
from pymongo.database import Database
from pymongo.change_stream import CollectionChangeStream
from apscheduler.jobstores.base import JobLookupError

from api_utils import users, questions
from singleton import scheduler
from constants import BATTLE_ONE_VS_ONE, DBNAME
from constants import COINS_POOL_ONE_VS_ONE, SCORE_WAIT_INTERVAL, STATUS_BUSY, STATUS_ONLINE
from constants import WAITING_ROOM, BATTLES
from utils import send_multi_message, current_milli_time

db: Database
wait_stream: CollectionChangeStream
battle_stream: CollectionChangeStream


def init():
    client = MongoClient(
        "mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
    global db
    db = client[DBNAME]

    # db[WAITING_ROOM] should not be empty

    pipeline = [{"$match": {"operationType": {"$in": ['insert', 'delete']}}}]
    global wait_stream
    wait_stream = db[WAITING_ROOM].watch(pipeline)

    pipeline = [{"$match": {"operationType": {"$in": ['update']}}}]
    global battle_stream
    battle_stream = db[BATTLES].watch(pipeline=pipeline, full_document="updateLookup")


def join_waiting_room(uid):
    resp = {}

    coins = users.get_my_coins(uid)

    if coins < COINS_POOL_ONE_VS_ONE:
        resp['success'] = False
        resp['message'] = 'You do not have enough coins'
        return resp

    room = db[WAITING_ROOM].insert_one({"_id": uid})
    users.update_user_status(uid, STATUS_BUSY)

    if room is None:
        resp['success'] = False
        resp['message'] = 'Could not join waiting room'
    else:
        resp['success'] = True
    return resp


def leave_waiting_room(uid):
    resp = {}

    count = db[WAITING_ROOM].delete_one({"_id": uid}).deleted_count
    users.update_user_status(uid, STATUS_ONLINE)

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

                random_users = [users.get_player_profile(user['_id']) for user in random_users]

                uids = [user['_id'] for user in random_users]

                db[WAITING_ROOM].delete_many({"_id": {"$in": random_users}})
                players = [{"uid": user['_id'], "score": -1, "user_name": user['user_name'],
                            "avatar": user['avatar'], "level": user['level']}
                           for user in random_users]

                battle = {
                    "_id": str(uuid.uuid4()),
                    "type": BATTLE_ONE_VS_ONE,
                    "start_time": current_milli_time(),
                    "coins_pool": COINS_POOL_ONE_VS_ONE,
                    "players": players
                }
                db[BATTLES].insert_one(battle)

                users.charge_entry_fee(uids, COINS_POOL_ONE_VS_ONE)

                for uid in uids:
                    users.update_user_status(uid, STATUS_BUSY)

                tokens = users.get_fcm_tokens(uids)

                db[WAITING_ROOM].remove({"_id": {"$in": uids}})

                random_questions = questions.get_random_questions(10)

                db[BATTLES].update_one(
                    {"_id": battle['_id']},
                    {"$set": {"questions": random_questions}}
                )

                battle['start_time'] = str(battle['start_time'])
                battle['coins_pool'] = str(battle['coins_pool'])
                battle['players'] = simplejson.dumps(battle['players'])

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
            {"$set": {"players.$.score": score, "endTime": current_milli_time()}},
            return_document=ReturnDocument.AFTER
        )

        print('battle scores = ', battle['players'])
        print('scores = ', list(filter(lambda player: player['score'] != -1, battle['players'])))
        updated = len(list(filter(lambda player: player['score'] != -1, battle['players'])))
        print('score updated for ', updated)

        if updated == 1:
            print('triggered score wait job')
            wait_for_score_updates(bid)
        else:
            print('score wait job already present')

        resp['success'] = True
    except PyMongoError as e:
        resp['success'] = False
        resp['message'] = str(e)
    finally:
        return resp


def wait_for_score_updates(bid):
    scheduler.add_job(func=send_score_updates, args=[bid], trigger="date", id=bid,
                      run_date=datetime.now() + timedelta(seconds=SCORE_WAIT_INTERVAL))


def send_score_updates(bid):
    players = db[BATTLES].find_one_and_update(
        {"_id": bid},
        {"$set": {"status": "finished"}},
        {"_id": 0, "players": 1}
    )["players"]

    uids = [player['uid'] for player in players]

    for uid in uids:
        users.update_user_status(uid, STATUS_ONLINE)

    users.update_stats_from_scores(COINS_POOL_ONE_VS_ONE, players)

    results = []

    for player in players:
        new_level, updated = users.update_level(player)
        results.append({
            "new_level": new_level,
            "updated": updated,
            "player": player
        })

    tokens = users.get_fcm_tokens(uids)

    data = {
        "type": "score-update",
        "battleId": bid,
        "payload": simplejson.dumps(results)
    }
    send_multi_message(data, tokens)


def watch_battles():
    print('starting watching battle scores')

    try:
        for change in battle_stream:
            print(change)
            battle = change['fullDocument']

            remaining = len(list(filter(lambda player: player['score'] == -1, battle['players'])))
            if remaining == 0:
                send_score_updates(battle['_id'])
                try:
                    scheduler.remove_job(job_id=battle['_id'])
                except JobLookupError as e:
                    print('job error=', e)

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)
