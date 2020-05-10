import pymongo

from data_collection.question_scrape import get_questions

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

QUESTIONS = 'questions'


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
