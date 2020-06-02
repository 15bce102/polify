import uuid

import simplejson
from apscheduler.jobstores.base import JobLookupError
from pymongo import MongoClient
from pymongo.errors import PyMongoError

from api_utils import users, questions
from constants import BATTLE_ONE_VS_ONE
from constants import COINS_POOL_ONE_VS_ONE
from constants import STATUS_BUSY, STATUS_ONLINE, STATUS_OFFLINE
from constants import WAITING_ROOM, BATTLES, DBNAME, USERS
from utils import send_multi_message, current_milli_time

client = MongoClient("mongodb+srv://polify:polify@cluster0-ht1fc.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

pipeline = [{"$match": {"operationType": {"$in": ['insert', 'delete']}}}]
wait_stream = db[WAITING_ROOM].watch(pipeline)

pipeline = [{"$match": {"operationType": {"$in": ['update']}}}]
battle_stream = db[BATTLES].watch(pipeline=pipeline, full_document="updateLookup")


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

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)


def watch_battles():
    print('starting watching battle scores')

    try:
        for change in battle_stream:
            battle = change['fullDocument']

            remaining = len(list(filter(lambda player: player['score'] == -1, battle['players'])))
            if remaining == 0:
                send_score_updates(battle['_id'])
                try:
                    print('removing job ', battle['_id'])
                    # TODO: scheduler.remove_job(job_id=battle['_id'])
                except JobLookupError as e:
                    print('job error=', e)

    except PyMongoError as e:
        # The ChangeStream encountered an unrecoverable error or the
        # resume attempt failed to recreate the cursor.
        print('stream error=', e)


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
    print('coins_updates = ', coins_updates)

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


def update_all_users():
    print('updating offline users')

    now = current_milli_time()
    interval = 2 * 60 * 1000

    count = db[USERS].update_many(
        {"status": STATUS_ONLINE, "last_seen": {"$lt": now - interval}},
        {"$set": {"status": STATUS_OFFLINE}}
    ).modified_count
    print('updated count = ', count)


def stop_matchmaking():
    wait_stream.close()


def auto_update_scores():
    print('auto updating scores....')
    now = current_milli_time()
    battles = list(db[BATTLES].find(
        {"status": "waiting", "end_time": {"$lte": now - 30 * 1000}}
    ))

    for battle in battles:
        send_score_updates(battle['_id'])


def end_battles():
    now = current_milli_time()
    battles = list(db[BATTLES].find(
        {"status": "progressing", "start_time": {"$lte": now - 200 * 1000}}
    ))

    for battle in battles:
        db[BATTLES].update_one(
            {"_id": battle['_id']},
            {"$set": {"status": "finished"}}
        )

        uids = [player['uid'] for player in battle['players']]
        for uid in uids:
            users.update_user_status(uid, STATUS_ONLINE)
