from pymongo import MongoClient
from pymongo.database import Database

from constants import QUESTIONS, DBNAME
from data_collection.question_scrape import get_questions
from datetime import datetime, timedelta

db: Database


def init():
    client = MongoClient(
        "mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
    global db
    db = client[DBNAME]


def insert_questions(url, category, que_date):
    questions = get_questions(url, category, que_date)
    db[QUESTIONS].insert_many(questions)


def get_random_questions(count):
    return list(db[QUESTIONS].aggregate([{"$sample": {"size": count}}]))


def populate_questions():
    url = "https://www.indiabix.com"
    category = "current-affairs"
    que_date = datetime.strptime('2020-01-02', '%Y-%d-%m').date()

    for i in range(0, 28):
        print('questions for :', str(que_date))
        insert_questions(url, category, str(que_date))
        que_date += timedelta(days=1)
