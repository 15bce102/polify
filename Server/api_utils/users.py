import pymongo
from pymongo import ReturnDocument

from utils import current_milli_time, get_user_from_phone_number

from constants import DBNAME, USERS
from constants import STATUS_ONLINE, STATUS_OFFLINE

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]


def create_user(uid):
    resp = {}

    user = db[USERS].find_one_and_update(
        {"_id": uid},
        {"$set": {"status": STATUS_ONLINE, "last_seen": current_milli_time()},
         "$setOnInsert": {"coins": 100, "level": "Rookie"}
         },
        upsert=True,
        return_document=ReturnDocument.AFTER
    )

    if user is None:
        resp['success'] = False
        resp['message'] = 'Database operation unsuccessful'
    else:
        resp['success'] = True
        resp['user'] = user

    return resp


def update_user_status(uid, status):
    resp = {}

    user = db[USERS].find_one_and_update(
        {"_id": uid},
        {"$set": {"status": status}},
        return_document=ReturnDocument.AFTER
    )

    if user is None:
        resp['success'] = False
        resp['message'] = 'Cannot update user status'
    else:
        resp['success'] = True

    return resp


def update_user_profile(uid, user_name, avatar):
    resp = {}

    user = db[USERS].update_one(
        {"_id": uid},
        {"$set": {"user_name": user_name, "avatar": avatar,
                  "status": STATUS_ONLINE, "last_seen": current_milli_time()
                  },
         "$setOnInsert": {"coins": 100, "level": "Rookie"}
         },
        upsert=True
    )

    print(user)

    if user is not None:
        resp['success'] = True
    else:
        resp['success'] = False
        resp['message'] = 'Cannot update user profile'

    print(resp)
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

    print('profile resp = ', resp)
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

    friends_uid = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "friends": 1}
    )['friends']

    print(friends_uid)

    friends = list(db[USERS].find(
        {"_id": {"$in": friends_uid}},
        {"status": 1, "user_name": 1, "avatar": 1}
    ))

    if friends is None:
        resp['success'] = False
        resp['message'] = 'Database operation error'
    else:
        resp['success'] = True
        resp['friends'] = friends

    return resp


def is_friend_request(uid, friend_uid):
    user = db[USERS].find_one(
        {"_id": uid, "requests": {"$in": [friend_uid]}}
    )

    if user is None:
        return False
    return True


def update_all_users():
    print('updating offline users')

    now = current_milli_time()
    interval = 2 * 60 * 1000

    count = db[USERS].update_many(
        {"status": STATUS_ONLINE, "last_seen": {"$lt": now - interval}},
        {"$set": {"status": STATUS_OFFLINE}}
    ).modified_count
    print('updated count = ', count)


def get_fcm_tokens(uids):
    tokens = list(db[USERS].find({"_id": {"$in": uids}}, {"_id": 0, "token": 1}))
    print(tokens)
    return [str(token['token']) for token in tokens]


def update_fcm_token(uid, token):
    resp = {}
    count = db[USERS].update_one({"_id": uid}, {"$set": {"token": token}}, upsert=True).matched_count

    if count == 1:
        resp['success'] = True
    else:
        resp['success'] = False
        resp['message'] = 'Could not update FCM token'

    return resp


def get_my_coins(uid):
    coins = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "coins": 1}
    )["coins"]
    return coins


def update_coins_from_scores(coins, players):
    resp = {}

    total_coins = coins * len(players)

    top_score = max(player['score'] for player in players)
    winners = [player['uid'] for player in players if player['score'] == top_score]

    db[USERS].update(
        {"_id": {"$in": winners}},
        {"$inc": {"coins": total_coins // len(winners)}}
    )

    resp['success'] = True
    return resp


def charge_entry_fee(uids, entry_fee):
    db[USERS].update_many(
        {"_id": {"$in": uids}},
        {"$inc": {"coins": -entry_fee}}
    )


def get_friends_from_phone_numbers(uid, phone_numbers):
    friends = filter(lambda x: x is not None,
                     [get_user_from_phone_number(number) for number in phone_numbers])
    print('friends=', friends)

    f_uids = [f['uid'] for f in friends]

    db[USERS].update_one(
        {"_id": uid},
        {"$addToSet": {"friends": f_uids}}
    )

    resp = {
        'success': True
    }
    return resp


def get_status(uid):
    status = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "status": 1}
    )['status']
    return status
