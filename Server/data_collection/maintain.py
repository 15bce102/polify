import pymongo

DBNAME = 'polify_db'

client = pymongo.MongoClient("mongodb+srv://polify:polify@cluster0-dhuyw.mongodb.net/test?retryWrites=true&w=majority")
db = client[DBNAME]

QUESTIONS = 'questions'


def insert_questions(que_list):
    db[QUESTIONS].insert_many(que_list)
