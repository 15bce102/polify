import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import auth
from firebase_admin.auth import UserNotFoundError

import uuid
import time

cred = credentials.Certificate('key.json')
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://quizwars-b6cca.firebaseio.com/'
})

users_ref = db.reference('users')
battles_ref = db.reference('battles')


def create_user(uid):
    if is_valid_user(uid):
        user = {
            "coins": 100
        }
        users_ref.child(uid).update(user)
    else:
        print(f"User {uid} already exists")


def create_battle(uid, coins):
    curr_user = users_ref.child(uid)

    user = curr_user.get()
    print(user)

    if user["coins"] < coins:
        print("Cannot create battle. Not enough coins")
        return

    battle_id = str(uuid.uuid4())
    battle = {
        "creator": uid,
        "time": current_milli_time(),
        "coins_pool": coins
    }
    battles_ref.child(battle_id).set(battle)


def join_battle(uid, battle_id, coins):
    curr_battle = battles_ref.child(battle_id)

    battle = curr_battle.get()
    print(battle)

    if battle["coins_pool"] > coins:
        print("Cannot join battle. Not enough coins")
        return

    curr_battle.child("members").push(uid)


def is_valid_user(uid):
    try:
        auth.get_user(uid)
    except UserNotFoundError as e:
        print(e)
        return False
    else:
        return True


def current_milli_time():
    return int(round(time.time() * 1000))
