import uuid

import simplejson
from pymongo import ReturnDocument, MongoClient
from pymongo.errors import PyMongoError
from time import sleep
from threading import Thread

from api_utils import users, questions
from constants import BATTLE_MULTIPLAYER, BATTLE_ONE_VS_ONE
from constants import COINS_POOL_ONE_VS_ONE, COINS_POOL_MULTIPLAYER
from constants import STATUS_BUSY, STATUS_ONLINE
from constants import WAITING_ROOM, BATTLES, PRIVATE_ROOM, DBNAME, USERS
from utils import send_multi_message, send_single_message, current_milli_time, bot_answer_algo

client = MongoClient("mongodb+srv://polify:polify@cluster0-ht1fc.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]


# db[WAITING_ROOM] should not be empty

def start_updating_bot_score(bid, bot_id):
    print('bot battle start')
    score = 0
    pos = 0

    battle = db[BATTLES].find_one({"_id": bid})

    for question in battle['questions']:
        pos += 1
        print('bot playing question {0}'.format(pos))
        if question['correctAnswer'] == bot_answer_algo():
            score += 1

        sleep(19)

    print('bot game finished')
    update_battle_score(battle['_id'], bot_id, score)


def create_and_start_battle(random_users, bot_id=None):
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
        {"$set": {"questions": random_questions, "status": "progressing"}}
    )

    battle['start_time'] = str(battle['start_time'])
    battle['coins_pool'] = str(battle['coins_pool'])
    battle['players'] = simplejson.dumps(battle['players'])

    data = {
        "type": "matchmaking",
        "payload": simplejson.dumps(battle)
    }
    send_multi_message(data, tokens)

    if bot_id is not None:
        thread = Thread(target=start_updating_bot_score, args=[battle['_id'], bot_id])
        thread.start()


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


def update_battle_score(bid, uid, score):
    resp = {}

    print('in update battle score')

    try:
        battle = db[BATTLES].find_one_and_update(
            {"_id": bid, "players.uid": uid},
            {"$set": {"players.$.score": score}},
            return_document=ReturnDocument.AFTER
        )

        print('battle scores = ', battle['players'])
        updated = len(list(filter(lambda player: player['score'] != -1, battle['players'])))
        print('score updated for ', updated)

        if updated == 1:
            db[BATTLES].update_one(
                {"_id": bid},
                {"$set": {"status": "waiting", "end_time": current_milli_time()}}
            )
            # TODO: wait_for_score_updates(bid)

        users.update_user_status(uid, STATUS_ONLINE)

        resp['success'] = True
    except PyMongoError as e:
        resp['success'] = False
        resp['message'] = str(e)
    finally:
        return resp


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

    users.update_user_status(uid, STATUS_BUSY)
    user = users.get_player_profile(uid)

    member = {
        "uid": user['_id'],
        "user_name": user['user_name'],
        "avatar": user['avatar'],
        "level": user['level'],
        "score": -1
    }

    room = {
        "_id": str(uuid.uuid4()),
        "owner": uid,
        "coins_pool": COINS_POOL_MULTIPLAYER,
        "createdAt": current_milli_time(),
        "members": [member]
    }
    try:
        db[PRIVATE_ROOM].update_one(
            {"_id": room['_id']},
            {"$set": {"owner": room['owner'], "coins_pool": room['coins_pool'], "createdAt": room['createdAt'],
                      "members": room['members']}},
            upsert=True)
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

    if room is None:
        resp['success'] = False
        resp['message'] = 'Room does not exist'

        print('Failed sending request to {0}'.format(f_uid))
        return resp
    elif users.get_user_status(f_uid) == STATUS_BUSY:
        resp['success'] = False
        resp['message'] = 'User is currently busy'

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

    users.update_user_status(uid, STATUS_BUSY)

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

    db[BATTLES].insert_one(battle)

    battle['start_time'] = str(battle['start_time'])
    battle['coins_pool'] = str(battle['coins_pool'])
    battle['players'] = str(battle['players'])

    uids = [member['uid'] for member in room['members']]

    users.charge_entry_fee(uids, COINS_POOL_MULTIPLAYER)

    tokens = users.get_fcm_tokens(uids)

    data = {
        "type": "start-match",
        "payload": simplejson.dumps(battle)
    }

    random_questions = questions.get_random_questions(10)

    try:
        db[PRIVATE_ROOM].delete_one({"_id": room_id})
        db[BATTLES].update_one(
            {"_id": battle['_id']},
            {"$set": {"status": 'progressing', "questions": random_questions}}
        )
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

        for u in uids:
            users.update_user_status(u, STATUS_ONLINE)

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

    users.update_user_status(uid, STATUS_ONLINE)

    data = {
        "type": "leave-room",
        "payload": simplejson.dumps(payload)
    }

    send_multi_message(data, tokens)

    resp['success'] = True

    return resp


def leave_battle(uid, bid):
    resp = {}

    try:
        db[BATTLES].update_one(
            {"_id": bid},
            {"$pull": {"players": {"uid": uid}}},
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Could not leave battle'

    return resp
