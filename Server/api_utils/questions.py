from pymongo import MongoClient
from pymongo.database import Database

from constants import QUESTIONS, DBNAME
from data_collection.question_scrape import get_questions

db: Database


def init():
    client = MongoClient(
        "mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
    global db
    db = client[DBNAME]


def insert_questions(url, category, date):
    questions = get_questions(url, category, date)
    db[QUESTIONS].insert_many(questions)


def get_random_questions(count):
    return list(db[QUESTIONS].aggregate([{"$sample": {"size": count}}]))


if __name__ == "__main__":
    url1 = "https://www.indiabix.com"
    category1 = "current-affairs"
    date1 = "2020-04-08"
    # insert_questions(url1, category1, date1)
