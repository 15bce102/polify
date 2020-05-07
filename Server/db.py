import firebase_admin
from firebase_admin import credentials
from firebase_admin import auth
from firebase_admin.auth import UserNotFoundError

import pymongo
import uuid
import time

cred = credentials.Certificate('key.json')
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://quizwars-b6cca.firebaseio.com/'
})

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

USERS = 'users'
BATTLES = 'battles'
QUESTIONS = 'questions'


def create_user(uid):
    # if not is_valid_user(uid):
    #     return

    user = {
        "_id": uid,
        "coins": 100
    }
    db[USERS].insert_one(user)


def create_battle(uid, coins):
    # if not is_valid_user(uid):
    #     return

    user = db[USERS].find({"_id": uid})[0]
    print(user)

    if user['coins'] < coins:
        print("Cannot create battle. Not enough coins")
        return

    battle_id = str(uuid.uuid4())
    battle = {
        "_id": battle_id,
        "creator": uid,
        "time": current_milli_time(),
        "coins_pool": coins
    }

    db[BATTLES].insert_one(battle)


def join_battle(uid, battle_id):
    # if not is_valid_user(uid):
    #     return

    user = db[USERS].find({"_id": uid})[0]
    battle = db[BATTLES].find({"_id": battle_id})[0]
    print(battle)

    if battle["coins_pool"] > user['coins']:
        print("Cannot join battle. Not enough coins")
        return

    db[BATTLES].update({"_id": battle_id}, {"$addToSet": {
        "members": uid
    }})


def insert_questions(que_list):
    db[QUESTIONS].insert_many(que_list)


def is_valid_user(uid):
    if db[USERS].find({"_id": uid}) is None:
        return False

    try:
        auth.get_user(uid)
    except UserNotFoundError as e:
        print(e)
        return False
    else:
        return True


def current_milli_time():
    return int(round(time.time() * 1000))
