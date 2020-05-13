import atexit

import firebase_admin
from firebase_admin import credentials

import simplejson

from api_utils.scheduling import scheduler

from flask import Flask, request

from api_utils import battles, users

from utils import current_milli_time, is_valid_user

app = Flask(__name__)

cred = credentials.Certificate('keys/key.json')
firebase_admin.initialize_app(cred)

scheduler.add_job(func=users.update_all_users, trigger="interval", seconds=3 * 60)
scheduler.add_job(func=battles.start_matchmaking)

scheduler.start()

# Shut down the scheduler when exiting the app
atexit.register(lambda: shut_down())


@app.route('/', methods=['GET'])
def welcome():
    resp = {
        "message": "Welcome to polify",
        "time": current_milli_time()
    }
    return simplejson.dumps(resp)


@app.route('/login', methods=['GET'])
def login_user():
    uid = request.args['uid']

    # valid, resp = is_valid_user(uid)
    # if not valid:
    #     return resp

    resp = users.create_user(uid)
    return simplejson.dumps(resp)


@app.route('/update-status', methods=['GET'])
def update_status():
    uid = request.args['uid']
    status = int(request.args['status'])

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.update_user_status(uid, status)
    return simplejson.dumps(resp)


@app.route('/update-profile', methods=['GET'])
def update_profile():
    uid = request.args['uid']
    user_name = request.args['user_name']
    avatar_uri = request.args['avatar_uri']

    print('checking user validity')
    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    print('now updating user profile')
    resp = users.update_user_profile(uid, user_name, avatar_uri)
    return simplejson.dumps(resp)


@app.route('/fetch-profile', methods=['GET'])
def fetch_profile():
    uid = request.args['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.fetch_user_profile(uid)
    return simplejson.dumps(resp)


@app.route('/update-token', methods=['GET'])
def update_token():
    uid = request.args['uid']
    token = request.args['token']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.update_fcm_token(uid, token)
    return simplejson.dumps(resp)


@app.route('/join-waiting-room', methods=['GET'])
def join_1v1_waiting_room():
    uid = request.args['uid']

    # valid, resp = is_valid_user(uid)
    # if not valid:
    #     return resp

    resp = battles.join_waiting_room(uid)
    return resp


@app.route('/leave-waiting-room', methods=['GET'])
def leave_1v1_waiting_room():
    uid = request.args['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = battles.leave_waiting_room(uid)
    return resp


@app.route('/get-questions', methods=['GET'])
def get_battle_questions():
    bid = request.args['bid']

    resp = battles.get_battle_questions(bid)
    return resp


@app.route('/update-score', methods=['GET'])
def update_score():
    bid = request.args['bid']
    uid = request.args['uid']
    score = int(request.args['score'])

    # valid, resp = is_valid_user(uid)
    # if not valid:
    #     return resp

    resp = battles.update_battle_score(bid, uid, score)
    return resp


def shut_down():
    try:
        battles.stop_matchmaking()
        scheduler.shutdown()
        print('shutdown success')
    except Exception as e:
        print("shutdown exception:", e)


if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)
