import time

from os import listdir
from os.path import isfile, join

from firebase_admin import auth
from firebase_admin.auth import UserNotFoundError
from firebase_admin.messaging import Message, MulticastMessage, send, send_multicast


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
    except UserNotFoundError as e:
        print(e)
        return None


def send_multi_message(data, tokens):
    print('sending multi message')
    message = MulticastMessage(data=data, tokens=tokens)
    response = send_multicast(message)
    print('{0} messages were sent successfully'.format(response.success_count))


def send_single_message(data, token):
    message = Message(data, token)
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
