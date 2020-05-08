import db
from flask import Flask, request
import simplejson

app = Flask(__name__)


@app.route('/', methods=['GET'])
def welcome():
    resp = {
        "message": "Welcome to polify",
        "time": db.current_milli_time()
    }
    return simplejson.dumps(resp)


@app.route('/login', methods=['GET'])
def login_user():
    uid = request.args['uid']
    resp = db.create_user(uid)
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
