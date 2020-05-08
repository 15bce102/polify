import db
from flask import Flask, request


# app = Flask(__name__)

# db.create_user('uid12')
# db.create_battle('uid123', 30)
# db.join_battle('uid11', '467fcce1-c003-418e-8131-f56f43706adb')

questions = [
    {
        "_id": "qid1",
        "category": "games",
        "text": "Who is best CoD player?",
        "options": [
            {
                "_id": 1,
                "opt": "Rohit"
            },
            {
                "_id": 2,
                "opt": "Ramandeep"
            },
            {
                "_id": 3,
                "opt": "Darren"
            },
            {
                "_id": 4,
                "opt": "xyz"
            }
        ],
        "correct": 3
    },
    {
        "_id": "qid2",
        "category": "games",
        "text": "Who is worst CoD player?",
        "options": [
            {
                "_id": 1,
                "opt": "Rohit"
            },
            {
                "_id": 2,
                "opt": "Ramandeep"
            },
            {
                "_id": 3,
                "opt": "Darren"
            },
            {
                "_id": 4,
                "opt": "xyz"
            }
        ],
        "correct": 2
    }
]

db.insert_questions(questions)

# if __name__ == "__main__":
#     app.run(host='0.0.0.0', debug=True)
