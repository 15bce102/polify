from singleton import db
from constants import QUESTIONS


def insert_questions(que_list):
    db[QUESTIONS].insert_many(que_list)
