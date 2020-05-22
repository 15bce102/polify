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
from constants import BATTLE_ONE_VS_ONE, BATTLE_MULTIPLAYER
from constants import COINS_POOL_ONE_VS_ONE, COINS_POOL_MULTIPLAYER
from constants import SCORE_WAIT_INTERVAL, STATUS_BUSY, STATUS_ONLINE
from constants import WAITING_ROOM, BATTLES, PRIVATE_ROOM, DBNAME, USERS
from utils import send_multi_message, send_single_message, current_milli_time

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

                print('random users = ', random_users)

                random_users = [users.get_player_profile(user['_id']) for user in random_users]

                print('random users profiles = ', random_users)

                uids = [user['_id'] for user in random_users]

                print('uids = ', uids)

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
        updated = len(list(filter(lambda player: player['score'] != -1, battle['players'])))
        print('score updated for ', updated)

        if updated == 1:
            print('triggered score wait job')
            wait_for_score_updates(bid)

        resp['success'] = True
    except PyMongoError as e:
        resp['success'] = False
        resp['message'] = str(e)
    finally:
        return resp


def wait_for_score_updates(bid):
    scheduler.add_job(func=send_score_updates, args=[bid], trigger="date", id=str(bid),
                      next_run_time=str(datetime.now() + timedelta(seconds=SCORE_WAIT_INTERVAL)))


def send_score_updates(bid):
    battle = db[BATTLES].find_one_and_update(
        {"_id": bid},
        {"$set": {"status": "finished"}},
        {"_id": 0, "players": 1, "type": 1}
    )

    players = battle["players"]

    uids = [player['uid'] for player in players]

    for uid in uids:
        users.update_user_status(uid, STATUS_ONLINE)

    coins_updates = users.update_stats_from_scores(players, battle['type'])

    results = []

    for player in players:
        new_level, updated = users.update_level(player)
        results.append({
            "new_level": new_level,
            "updated": updated,
            "coins": coins_updates[player['uid']],
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
            battle = change['fullDocument']

            remaining = len(list(filter(lambda player: player['score'] == -1, battle['players'])))
            if remaining == 0:
                send_score_updates(battle['_id'])
                try:
                    scheduler.remove_job(job_id=str(battle['_id']))
                except JobLookupError as e:
                    print('job error=', e)

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)


def create_room(uid):
    resp = {}

    coins = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "coins": 1}
    )['coins']

    if coins < COINS_POOL_MULTIPLAYER:
        resp['success'] = False
        resp['message'] = 'Not enough coins'
        return resp

    user = users.get_player_profile(uid)
    member = {
        "uid": user['_id'],
        "user_name": user['user_name'],
        "avatar": user['avatar'],
        "level": user['level'],
        "score": -1
    }

    room = {
        "_id": "multi_{0}".format(uid),
        "owner": uid,
        "coins_pool": COINS_POOL_MULTIPLAYER,
        "createdAt": current_milli_time(),
        "members": [member]
    }
    try:
        db[PRIVATE_ROOM].insert_one(room)
        resp['success'] = True
        resp['room'] = room
    except PyMongoError as e:
        resp['success'] = False
        resp['message'] = str(e)

    return resp


def send_room_invite(f_uid, room_id):
    resp = {}

    room = db[PRIVATE_ROOM].find_one({"_id": room_id})
    token = users.get_fcm_tokens([f_uid])[0]

    print('sending request to {0}'.format(f_uid))

    if room is None or token is None:
        resp['success'] = False
        resp['message'] = 'Room does not exist'

        print('Failed sending request to {0}'.format(f_uid))

        return resp

    data = {
        "type": "invite",
        "payload": simplejson.dumps(room)
    }

    send_single_message(data, token)

    print('single {0}'.format(f_uid))

    resp['success'] = True
    return resp


def join_private_room(uid, room_id):
    resp = {}

    coins = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "coins": 1}
    )['coins']

    if coins < COINS_POOL_MULTIPLAYER:
        resp['success'] = False
        resp['message'] = 'You do not have enough coins'
        return resp

    user = users.get_player_profile(uid)
    member = {
        "uid": user['_id'],
        "user_name": user['user_name'],
        "avatar": user['avatar'],
        "level": user['level'],
        "score": -1
    }

    room = db[PRIVATE_ROOM].find_one({"_id": room_id})

    if room is None:
        resp['success'] = False
        resp['message'] = 'Room does not exist'
        return resp
    elif len(list(room['members'])) == 4:
        resp['success'] = False
        resp['message'] = 'Maximum limit reached'
        return resp

    room = db[PRIVATE_ROOM].find_one_and_update(
        {"_id": room_id},
        {"$addToSet": {"members": member}},
        return_document=ReturnDocument.AFTER
    )

    resp['success'] = True
    resp['room'] = room

    uids = [member['uid'] for member in room['members']]
    uids.remove(uid)

    payload = {
        "message": '{0} joined'.format(user['user_name']),
        "room": simplejson.dumps(room)
    }

    tokens = users.get_fcm_tokens(uids)
    data = {
        "type": "multiplayer-join",
        "payload": simplejson.dumps(payload)
    }

    send_multi_message(data, tokens)

    return resp


def start_private_battle(uid, room_id):
    resp = {}

    room = db[PRIVATE_ROOM].find_one({"_id": room_id})

    if room is None:
        resp['success'] = False
        resp['message'] = 'Room does not exist'
        return resp
    elif len(room['members']) < 2:
        resp['success'] = False
        resp['message'] = 'You need at least two players to start the battle'
        return resp

    print('owner = {0}, uid = {1}'.format(room['owner'], uid))

    if room['owner'] != uid:
        resp['success'] = False
        resp['message'] = 'Only owner can start the match'
        return resp

    battle = {
        "_id": str(uuid.uuid4()),
        "type": BATTLE_MULTIPLAYER,
        "owner": room['owner'],
        "start_time": current_milli_time(),
        "coins_pool": COINS_POOL_MULTIPLAYER,
        "players": room['members']
    }

    new_battle = battle
    new_battle['start_time'] = str(new_battle['start_time'])
    new_battle['coins_pool'] = str(new_battle['coins_pool'])
    new_battle['players'] = str(new_battle['players'])

    uids = [member['uid'] for member in room['members']]

    users.charge_entry_fee(uids, COINS_POOL_MULTIPLAYER)

    tokens = users.get_fcm_tokens(uids)

    data = {
        "type": "start-match",
        "payload": simplejson.dumps(new_battle)
    }

    random_questions = questions.get_random_questions(10)
    battle['questions'] = random_questions

    try:
        db[PRIVATE_ROOM].delete_one({"_id": room_id})
        db[BATTLES].insert_one(battle)
    except PyMongoError as e:
        resp['success'] = False
        resp['message'] = str(e)
        return resp

    send_multi_message(data, tokens)

    resp['success'] = True
    return resp


def leave_private_room(uid, room_id):
    resp = {}

    room = db[PRIVATE_ROOM].find_one({"_id": room_id})

    if room is None:
        resp['success'] = False
        resp['message'] = 'Room does not exist'
        return resp

    uids = [member['uid'] for member in room['members']]
    uids.remove(uid)

    payload = {
        "room_id": room_id
    }

    tokens = users.get_fcm_tokens(uids)

    if room['owner'] == uid:
        db[PRIVATE_ROOM].delete_one({"_id": room_id})

        owner = users.get_player_profile(uid)

        payload['message'] = "Owner {0} deleted the room".format(owner['user_name'])
        payload['room'] = None

    else:
        user = users.get_player_profile(uid)

        room = db[PRIVATE_ROOM].find_one_and_update(
            {"_id": room_id},
            {"$pull": {"members": {"uid": uid}}},
            return_document=ReturnDocument.AFTER
        )

        payload['message'] = "{0} has left the room".format(user['user_name'])
        payload['room'] = simplejson.dumps(room)

    data = {
        "type": "leave-room",
        "payload": simplejson.dumps(payload)
    }

    send_multi_message(data, tokens)

    resp['success'] = True

    return resp
