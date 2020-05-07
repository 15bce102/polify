import db as db
from flask import Flask, request


# app = Flask(__name__)

db.create_user('uid12')
db.create_battle('uid123', 30)
db.join_battle('uid11', '467fcce1-c003-418e-8131-f56f43706adb')

# if __name__ == "__main__":
#     app.run(host='0.0.0.0', debug=True)
