import uuid

import pymongo
from pymongo import ReturnDocument

from utils import is_valid_user
from utils import current_milli_time



DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

USERS = 'users'
BATTLES = 'battles'
QUESTIONS = 'questions'




def create_battle(uid, coins):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    user = db[USERS].find_one({"_id": uid})

    if user['coins'] < coins:
        resp['success'] = False
        resp['message'] = "Cannot create battle. Not enough coins"
        return resp

    battle_id = str(uuid.uuid4())
    battle = {
        "_id": battle_id,
        "creator": uid,
        "started": False,
        "time": current_milli_time(),
        "coins_pool": coins,
        "members": [{
            "uid": uid,
            "score": -1
        }]
    }

    db[BATTLES].insert_one(battle)

    resp['battle'] = battle
    return resp


def join_battle(uid, battle_id):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    user = db[USERS].find_one({"_id": uid})
    battle = db[BATTLES].find_one({"_id": battle_id})

    if battle["coins_pool"] > user['coins']:
        resp['success'] = False
        resp['message'] = "Cannot join battle. Not enough coins"
        return resp

    db[BATTLES].update({"_id": battle_id}, {"$addToSet": {
        "members": {
            "uid": uid,
            "score": -1
        }
    }})

    resp['battle'] = db[BATTLES].find_one({"_id": battle_id})

    return resp


def start_battle(battle_id, uid):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    battle = db[BATTLES].find_one({"_id": battle_id})

    if not battle['started'] and battle['creator'] == uid:
        db[BATTLES].update({"_id": battle_id}, {"started": True})

        resp['battle'] = db[BATTLES].find({"_id": battle_id})[0]
        return resp

    else:
        resp['success'] = False
        resp['message'] = "Battle already started or you are not the creator"
        return resp


def my_rooms(uid, page_start, page_size):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    query = {"members.uid": uid, "started": False}

    total = db[BATTLES].find(query).count()

    rooms = db[BATTLES].find(query).skip(page_start).limit(min(page_size, total - page_start))
    resp['rooms'] = list(rooms)
    print(resp['rooms'])

    if page_start + page_size < total:
        resp['hasMore'] = True
    else:
        resp['hasMore'] = False

    resp['success'] = True
    return resp
