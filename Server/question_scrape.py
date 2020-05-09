import pprint
import uuid

import requests
from bs4 import BeautifulSoup


def get_questions(url, category, date):
    url_complete = url + "/" + category + "/" + date + "/"
    html = requests.get(url_complete).content
    soup = BeautifulSoup(html, "html.parser")
    tables = soup.find_all('div', class_='bix-div-container')
    questions = []
    for i in tables:
        json_data = {'_id': uuid.uuid4().hex[:8], 'category': category}

        ques = i.find('td', class_='bix-td-qtxt').text
        json_data['question'] = ques

        correct_answer = i.find('span', class_='jq-hdnakqb mx-bold').text
        json_data['correctAnswer'] = correct_answer
        options = i.find_all('td', attrs={"class": "bix-td-option", "width": "99%"})
        json_data['options'] = []
        ch = 'A'
        for j in options:
            json_data['options'].append({'id': ch, 'opt': j.text})
            ch = chr(ord(ch) + 1)
        questions.append(json_data)
    return questions


def main():
    url = "https://www.indiabix.com"
    category = "current-affairs"
    date = "2020-04-08"
    questions = get_questions(url, category, date)
    pp = pprint.PrettyPrinter(indent=4)
    pp.pprint(questions)


if __name__ == "__main__":
    main()
