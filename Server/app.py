import atexit

import firebase_admin
import simplejson
from apscheduler.schedulers.background import BackgroundScheduler
from firebase_admin import credentials
from flask import Flask, request

from api_utils import battles
from api_utils import users
from utils import current_milli_time
from utils import is_valid_user

app = Flask(__name__)


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
    status = int(request.args['status'])

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.update_user_status(uid, status)
    return simplejson.dumps(resp)


def shut_down():
    try:
        battles.stop_matchmaking()
        scheduler.shutdown()
        print('shutdown success')
    except Exception as e:
        print("shutdown exception:", e)


if __name__ == "__main__":

    cred = credentials.Certificate('keys/key.json')
    firebase_admin.initialize_app(cred)

    scheduler = BackgroundScheduler()
    scheduler.add_job(func=users.update_all_users, trigger="interval", seconds=3 * 60)
    scheduler.add_job(func=battles.start_matchmaking)

    scheduler.start()

    # Shut down the scheduler when exiting the app
    atexit.register(lambda: shut_down())

    app.run(host='0.0.0.0', debug=False)
