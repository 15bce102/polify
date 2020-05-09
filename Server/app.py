import firebase_admin
import simplejson
from firebase_admin import credentials
from flask import Flask, request

import db
import users
from utils import current_milli_time
from utils import is_valid_user

app = Flask(__name__)

cred = credentials.Certificate('key.json')
firebase_admin.initialize_app(cred)


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

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.create_user(uid)
    return simplejson.dumps(resp)


@app.route('/update-status', methods=['GET'])
def update_status():
    uid = request.args['uid']
    online = False
    if request.args['online'] == 'true':
        online = True

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.update_user_status(uid, online)
    return simplejson.dumps(resp)


@app.route('/send-request', methods=['GET'])
def send_friend_request():
    uid = request.args['uid']
    friend_uid = request.args['friend_uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    valid, resp = is_valid_user(friend_uid)
    if not valid:
        return resp

    resp = users.send_friend_request(uid, friend_uid)
    return simplejson.dumps(resp)


@app.route('/accept-request', methods=['GET'])
def accept_request():
    uid = request.args['uid']
    friend_uid = request.args['friend_uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    valid, resp = is_valid_user(friend_uid)
    if not valid:
        return resp

    resp = users.accept_friend_request(uid, friend_uid)
    return simplejson.dumps(resp)


@app.route('/my-friends', methods=['GET'])
def my_friends():
    uid = request.args['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.get_my_friends(uid)
    return simplejson.dumps(resp)


@app.route('/create-battle', methods=['GET'])
def create_battle():
    uid = request.args['uid']
    coins = int(request.args['coins'])
    resp = db.create_battle(uid, coins)
    return simplejson.dumps(resp)


@app.route('/join-battle', methods=['GET'])
def join_battle():
    uid = request.args['uid']
    bid = request.args['bid']
    resp = db.join_battle(uid, bid)
    return simplejson.dumps(resp)


@app.route('/my-rooms', methods=['GET'])
def my_rooms():
    uid = request.args['uid']
    page_start = int(request.args['page_start'])
    page_size = int(request.args['page_size'])
    resp = db.my_rooms(uid, page_start, page_size)
    return simplejson.dumps(resp)


if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)
