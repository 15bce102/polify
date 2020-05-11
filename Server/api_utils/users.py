import pymongo
from pymongo import ReturnDocument

from utils import current_milli_time

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

USERS = 'users'

STATUS_OFFLINE = 0
STATUS_ONLINE = 1
STATUS_BUSY = 2
STATUS_WAITING = 3


def create_user(uid):
    resp = {}

    user = db[USERS].find_one_and_update(
        {"_id": uid},
        {"$set": {"status": STATUS_ONLINE, "last_seen": current_milli_time()},
         "$setOnInsert": {"coins": 100}},
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

    friends_uid = db[USERS].find_one(
        {"_id": uid},
        {"_id": 0, "friends": 1}
    )['friends']

    print(friends_uid)

    friends = list(db[USERS].find(
        {"_id": {"$in": friends_uid}},
        {"status": 1}
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
    tokens = db[USERS].find({"_id": {"$in": uids}}, {"_id:0", "token:1"})
    return tokens


def update_fcm_token(uid, token):
    resp = {}
    count = db[USERS].update_one({"_id": uid}, {"$set": {"token": token}}).matched_count

    if count == 1:
        resp['success'] = True
    else:
        resp['success'] = False
        resp['message'] = 'Could not update FCM token'

    return resp
