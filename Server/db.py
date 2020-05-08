import uuid
import pymongo

import firebase_admin
from firebase_admin import credentials

from utils import is_valid_user
from utils import current_milli_time

cred = credentials.Certificate('key.json')
firebase_admin.initialize_app(cred)

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

USERS = 'users'
BATTLES = 'battles'
QUESTIONS = 'questions'


def create_user(uid):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    db[USERS].update({"_id": uid}, {"$setOnInsert": {"coins": 100}}, upsert=True)

    resp['user'] = db[USERS].find({"_id": uid})[0]
    return resp


def create_battle(uid, coins):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    user = db[USERS].find({"_id": uid})[0]

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
        "coins_pool": coins
    }

    db[BATTLES].insert_one(battle)

    resp['battle'] = battle
    return resp


def join_battle(uid, battle_id):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    user = db[USERS].find({"_id": uid})[0]
    battle = db[BATTLES].find({"_id": battle_id})[0]

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

    resp['battle'] = db[BATTLES].find({"_id": battle_id})[0]

    return resp


def start_battle(battle_id, uid):
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    battle = db[BATTLES].find({"_id": battle_id})[0]

    if not battle['started'] and battle['creator'] == uid:
        db[BATTLES].update({"_id": battle_id}, {"started": True})

        resp['battle'] = db[BATTLES].find({"_id": battle_id})[0]
        return resp

    else:
        resp['success'] = False
        resp['message'] = "Battle already started or you are not the creator"
        return resp
