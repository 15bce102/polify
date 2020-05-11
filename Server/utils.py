import time

from firebase_admin import auth
from firebase_admin.auth import UserNotFoundError


def current_milli_time():
    return int(round(time.time() * 1000))


def is_valid_user(uid):
    resp = {}

    # try:
    #     auth.get_user(uid)
    # except UserNotFoundError as e:
    #     resp['success'] = False
    #     resp['message'] = str(e)
    #     return False, resp
    # else:
    resp['success'] = True
    return True, resp
