import time

from os import listdir
from os.path import isfile, join
import random

from firebase_admin import auth
from firebase_admin.auth import UserNotFoundError
from firebase_admin.exceptions import InvalidArgumentError
from firebase_admin.messaging import Message, MulticastMessage, send, send_multicast

from constants import LIST_BOT_IDS


def bot_answer_algo():
    return 'C'


def current_milli_time():
    return int(round(time.time() * 1000))


def is_valid_user(uid):
    resp = {}

    try:
        auth.get_user(uid)
    except UserNotFoundError as e:
        resp['success'] = False
        resp['message'] = str(e)
        return False, resp
    else:
        resp['success'] = True
        return True, resp


def get_user_from_phone_number(phone_number):
    try:
        user = auth.get_user_by_phone_number(phone_number)
        return user
    except (UserNotFoundError, InvalidArgumentError):
        return None


def send_multi_message(data, tokens):
    message = MulticastMessage(data=data, tokens=tokens)
    response = send_multicast(message)
    print('Messages sent: failed = {0}, success = {1}, other fields = {2}'
          .format(response.failure_count, response.success_count, [r.exception for r in response.responses]))


def send_single_message(data, token):
    message = Message(data=data, token=token)
    response = send(message)
    print('Successfully sent message:', response)


def get_avatars():
    path = 'avatars'
    avatars = [f for f in listdir(path) if isfile(join(path, f))]

    resp = {
        "success": True,
        "avatars": ['avatars/' + a for a in avatars]
    }
    return resp


def upgrade_tier(score):
    if score < 10:
        return "Newbie"
    elif 10 <= score < 30:
        return "Veteran 1"
    elif 30 <= score < 75:
        return "Veteran 2"
    elif 75 <= score < 150:
        return "Elite 1"
    elif 150 <= score < 300:
        return "Elite 2"
    else:
        return "Professional"


def get_random_bot():
    return random.choice(LIST_BOT_IDS)
