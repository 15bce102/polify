import firebase_admin
from firebase_admin import credentials
from flask import Flask, request, send_from_directory, send_file


import utils
from api_utils import battles, users
from constants import STATUS_BUSY, STATUS_ONLINE
from utils import current_milli_time, is_valid_user

app = Flask(__name__, static_url_path='')

cred = credentials.Certificate('keys/key.json')
firebase_admin.initialize_app(cred)


@app.route('/', methods=['GET'])
def welcome():
    resp = {
        "message": "Welcome to polify",
        "time": current_milli_time()
    }
    return resp


@app.route('/policy')
def privacy_policy():
    return send_file('policy.md')


"""User related requests"""


@app.route('/check-user-exists', methods=['POST'])
def check_number():
    number = request.json['phoneNumber']
    user = utils.get_user_from_phone_number(number)

    resp = {}

    if user is None:
        resp['success'] = False
        resp['message'] = 'User not found'
    else:
        resp['success'] = True

    return resp


@app.route('/signup', methods=['POST'])
def signup_user():
    uid = request.json['uid']
    avatar = request.json['avatar']
    user_name = request.json['user_name']
    token = request.json['token']

    resp = users.create_user(uid, avatar, user_name, token)
    return resp


@app.route('/login', methods=['POST'])
def login_user():
    uid = request.json['uid']
    token = request.json['token']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.login(uid, token)
    return resp


@app.route('/update-token', methods=['POST'])
def update_token():
    uid = request.json['uid']
    token = request.json['token']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.update_fcm_token(uid, token)
    return resp


@app.route('/update-status', methods=['POST'])
def update_status():
    uid = request.json['uid']
    status = int(request.args['status'])

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    if users.get_status(uid) == STATUS_BUSY and status == STATUS_ONLINE:
        resp = {
            "success": False,
            "message": "Busy status can be changed to online only by the server"
        }
        return resp

    resp = users.update_user_status(uid, status)
    return resp


@app.route('/update-avatar', methods=['POST'])
def update_profile():
    uid = request.json['uid']
    avatar = request.json['avatar']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.update_user_avatar(uid, avatar)
    return resp


@app.route('/fetch-profile', methods=['POST'])
def fetch_profile():
    uid = request.json['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.fetch_user_profile(uid)
    return resp


@app.route('/get-avatars', methods=['GET'])
def get_avatar_url():
    resp = utils.get_avatars()
    return resp


@app.route('/avatars/<path:path>')
def send_avatar_img(path):
    return send_from_directory('avatars', path)


@app.route('/my-friends', methods=['POST'])
def my_friends():
    uid = request.json['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.get_my_friends(uid)
    return resp


@app.route('/update-friends', methods=['POST'])
def update_friends():
    uid = request.json['uid']
    phone_numbers = request.json['phoneNumbers']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    print("uid={0}, contacts={1}".format(uid, len(phone_numbers)))

    resp = users.get_friends_from_phone_numbers(uid, phone_numbers)
    return resp


@app.route('/add-coins', methods=['POST'])
def add_coins():
    uid = request.json['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = users.add_coins(uid)
    return resp


"""Battle related requests"""


@app.route('/play-with-bot', methods=['POST'])
def play_with_bot():
    uid = request.json['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    bot_id = utils.get_random_bot()

    game_players = [users.get_player_profile(uid), users.get_player_profile(bot_id)]

    battles.create_and_start_battle(game_players, bot_id)

    resp = {"success": True}
    return resp


@app.route('/join-waiting-room', methods=['GET'])
def join_1v1_waiting_room():
    uid = request.args['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

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

    print('bid={0}, uid={1}, score={2}'.format(bid, uid, score))

    # valid, resp = is_valid_user(uid)
    # if not valid:
    #     return resp

    resp = battles.update_battle_score(bid, uid, score)
    return resp


@app.route('/leave-battle', methods=['POST'])
def leave_battle():
    uid = request.json['uid']
    bid = request.json['bid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = battles.leave_battle(uid, bid)
    return resp


@app.route('/create-room', methods=['POST'])
def create_room():
    uid = request.json['uid']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = battles.create_room(uid)
    return resp


@app.route('/send-invite', methods=['POST'])
def send_invite():
    print('send_invite json = ', request.json)
    uid = request.json['uid']
    f_uid = request.json['f_uid']
    room_id = request.json['room_id']

    print('{0} inviting {1}'.format(uid, f_uid))

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp
    valid, resp = is_valid_user(f_uid)
    if not valid:
        return resp

    resp = battles.send_room_invite(f_uid, room_id)
    return resp


@app.route('/join-room', methods=['POST'])
def join_room():
    uid = request.json['uid']
    room_id = request.json['room_id']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = battles.join_private_room(uid, room_id)
    return resp


@app.route('/leave-room', methods=['POST'])
def leave_room():
    uid = request.json['uid']
    room_id = request.json['room_id']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = battles.leave_private_room(uid, room_id)
    return resp


@app.route('/start-battle', methods=['POST'])
def start_battle():
    uid = request.json['uid']
    room_id = request.json['room_id']

    valid, resp = is_valid_user(uid)
    if not valid:
        return resp

    resp = battles.start_private_battle(uid, room_id)
    return resp


if __name__ == "__main__":
    app.run(debug=False)
