from datetime import datetime, timedelta

from pymongo import MongoClient

from constants import QUESTIONS, DBNAME
from data_collection.question_scrape import get_questions

client = MongoClient("mongodb+srv://polify:polify@cluster0-ht1fc.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]


def insert_questions(url, category, que_date):
    questions = get_questions(url, category, que_date)
    db[QUESTIONS].insert_many(questions)
    print('inserted {0} questions for {1}'.format(len(questions), que_date))


def get_random_questions(count):
    return list(db[QUESTIONS].aggregate([{"$sample": {"size": count}}]))


def populate_questions():
    url = "https://www.indiabix.com"
    category = "current-affairs"
    que_date = datetime.strptime('2020-01-05', '%Y-%d-%m').date()

    for i in range(0, 26):
        print('questions for :', str(que_date))
        insert_questions(url, category, str(que_date))
        que_date += timedelta(days=1)
