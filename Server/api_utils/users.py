from pymongo import ReturnDocument, MongoClient
from pymongo.errors import PyMongoError

from constants import STATUS_ONLINE, DBNAME, COINS_AD_VIEWING
from constants import USERS, COINS_INITIAL, COINS_POOL_MULTIPLAYER, COINS_POOL_ONE_VS_ONE, BATTLE_ONE_VS_ONE
from utils import current_milli_time, get_user_from_phone_number, upgrade_tier

client = MongoClient("mongodb+srv://polify:polify@cluster0-ht1fc.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]


def create_user(uid, avatar, user_name, token):
    resp = {}

    user = {
        "_id": uid,
        "user_name": user_name,
        "avatar": avatar,
        "token": token,
        "friends": [],
        "status": STATUS_ONLINE,
        "last_seen": current_milli_time(),
        "coins": COINS_INITIAL,
        "level": upgrade_tier(0),
        "totalScore": 0
    }

    try:
        db[USERS].insert_one(user)
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Profile already exists'

    return resp


def login(uid, token):
    resp = {}

    try:
        db[USERS].update_one(
            {"_id": uid},
            {"$set": {"status": STATUS_ONLINE, "last_seen": current_milli_time(), "token": token}},
            upsert=False
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'User does not exist'

    return resp


def update_user_status(uid, status):
    resp = {}

    try:
        db[USERS].update_one(
            {"_id": uid},
            {"$set": {"status": status}},
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Cannot update user status'

    return resp


def update_user_avatar(uid, avatar):
    resp = {}

    try:
        db[USERS].update_one(
            {"_id": uid},
            {"$set": {
                "avatar": avatar,
                "status": STATUS_ONLINE,
                "last_seen": current_milli_time()
            }}
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Cannot update user profile'

    return resp


def fetch_user_profile(uid):
    resp = {}

    user = db[USERS].find_one(
        {"_id": uid},
        {"_id": 1, "user_name": 1, "level": 1, "coins": 1, "avatar": 1}
    )

    if user is None:
        resp['success'] = False
        resp['message'] = 'Could not fetch profile'
    else:
        resp['success'] = True
        resp['user'] = user

    return resp


def send_friend_request(uid, friend_uid):
    resp = {}

    user = db[USERS].find_one_and_update(
        {"_id": friend_uid},
        {"$addToSet": {"requests": uid}},
        return_document=ReturnDocument.AFTER
    )

    if user is None:
        resp['success'] = False
        resp['message'] = 'Database operation unsuccessful'
    else:
        resp['success'] = True

    return resp


def accept_friend_request(uid, friend_uid):
    resp = {}

    if not is_friend_request(uid, friend_uid):
        resp['success'] = False
        resp['message'] = 'Friend request does not exist'
        return resp

    user = db[USERS].find_one_and_update(
        {"_id": uid},
        {"$addToSet": {"friends": friend_uid}, "$pull": {"requests": friend_uid}},
        return_document=ReturnDocument.AFTER
    )

    friend = db[USERS].find_one_and_update(
        {"_id": friend_uid},
        {"$addToSet": {"friends": uid}, "$pull": {"requests": uid}},
        return_document=ReturnDocument.AFTER
    )

    if user is None or friend is None:
        resp['success'] = False
        resp['message'] = 'Database operation unsuccessful'
    else:
        resp['success'] = True

    return resp


def get_my_friends(uid):
    resp = {}

    try:
        friends_uid = db[USERS].find_one(
            {"_id": uid},
            {"_id": 0, "friends": 1}
        )['friends']

        friends = list(db[USERS].find(
            {"_id": {"$in": friends_uid}},
            {"status": 1, "user_name": 1, "avatar": 1, "level": 1}
        ))

        resp['success'] = True
        resp['friends'] = friends
    except KeyError:
        resp['success'] = True
        resp['friends'] = []
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Could not find friends'

    return resp


def is_friend_request(uid, friend_uid):
    user = db[USERS].find_one(
        {"_id": uid, "requests": {"$in": [friend_uid]}}
    )

    if user is None:
        return False
    return True


def get_fcm_tokens(uids):
    tokens = list(db[USERS].find({"_id": {"$in": uids}}, {"_id": 0, "token": 1}))
    return [str(token['token']) for token in tokens]


def update_fcm_token(uid, token):
    resp = {}

    try:
        db[USERS].update_one(
            {"_id": uid},
            {"$set": {"token": token}}
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Could not update FCM token'

    return resp


def get_my_coins(uid):
    coins = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "coins": 1}
    )["coins"]
    return coins


def update_stats_from_scores(players, battle_type):
    if battle_type == BATTLE_ONE_VS_ONE:
        coins = COINS_POOL_ONE_VS_ONE
    else:
        coins = COINS_POOL_MULTIPLAYER

    total_coins = coins * len(players)

    top_score = max(player['score'] for player in players)
    winners = [player['uid'] for player in players if player['score'] == top_score]

    coins_update = {}
    for player in players:
        if player['uid'] in winners:
            coins_update["{0}".format(player['uid'])] = '+{0}'.format(total_coins // len(winners) - coins)
        else:
            coins_update["{0}".format(player['uid'])] = '-{0}'.format(coins)

    db[USERS].update_many(
        {"_id": {"$in": winners}},
        {"$inc": {"coins": total_coins // len(winners)}}
    )

    return coins_update


def charge_entry_fee(uids, entry_fee):
    db[USERS].update_many(
        {"_id": {"$in": uids}},
        {"$inc": {"coins": -entry_fee}}
    )


def get_friends_from_phone_numbers(uid, phone_numbers):
    resp = {}

    friends = list(filter(lambda x: x is not None,
                          [get_user_from_phone_number(number) for number in phone_numbers]))
    f_uids = [f.uid for f in friends]

    try:
        db[USERS].update_one(
            {"_id": uid},
            {"$addToSet": {"friends": {"$each": f_uids}}}
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Could not update friends'

    return resp


def get_status(uid):
    status = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "status": 1}
    )['status']
    return status


def update_level(player):
    user = db[USERS].find_one(
        {"_id": player['uid']},
        {"totalScore": 1, "level": 1}
    )

    inc = lambda x: x if x != -1 else 0

    new_score = user['totalScore'] + inc(player['score'])

    new_tier = upgrade_tier(new_score)
    old_tier = user['level']

    db[USERS].update_one(
        {"_id": player['uid']},
        {"$set": {"totalScore": new_score, "level": new_tier}}
    )

    if old_tier == new_tier:
        return new_tier, False
    else:
        return new_tier, True


def get_player_profile(uid):
    user = db[USERS].find_one(
        {"_id": uid},
        {"user_name": 1, "avatar": 1, "level": 1}
    )
    return user


def get_user_status(uid):
    user = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "status": 1}
    )
    return user['status']


def add_coins(uid):
    resp = {}

    try:
        db[USERS].update_one(
            {"_id": uid},
            {"$inc": {"coins": COINS_AD_VIEWING}}
        )
        resp['success'] = True
    except PyMongoError:
        resp['success'] = False
        resp['message'] = 'Could not add coins'

    return resp
