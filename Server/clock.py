import logging
from datetime import datetime, timedelta

from apscheduler.schedulers.background import BlockingScheduler
import firebase_admin
from firebase_admin import credentials

import jobs
from api_utils import questions
from constants import SCORE_WAIT_INTERVAL

cred = credentials.Certificate('keys/key.json')
firebase_admin.initialize_app(cred)

scheduler = BlockingScheduler()

logging.basicConfig()
logging.getLogger('apscheduler').setLevel(logging.DEBUG)

print('adding jobs')

scheduler.add_job(func=jobs.update_all_users, trigger="interval", minutes=2, id='update_status_job',
                  replace_existing=True)
scheduler.add_job(func=jobs.start_matchmaking, id='matchmaking_job',
                  replace_existing=True)
scheduler.add_job(func=jobs.watch_battles, id='watch_scores_job',
                  replace_existing=True)
scheduler.add_job(func=jobs.auto_update_scores, trigger='interval', seconds=20, id='auto_update_score_job',
                  replace_existing=True)
scheduler.add_job(func=jobs.end_battles, trigger='interval', minutes=2, id='end_battles_job',
                  replace_existing=True)
scheduler.add_job(func=questions.populate_questions, trigger="date",
                  run_date=datetime.strptime('May 27 2020  7:40PM', '%b %d %Y %I:%M%p'),
                  id='questions_populate_job', replace_existing=True)


def wait_for_score_updates(bid):
    print('wait for score updates')
    job = scheduler.add_job(func=jobs.send_score_updates, args=[bid], trigger="date", id=bid,
                            next_run_time=datetime.now() + timedelta(seconds=SCORE_WAIT_INTERVAL))
    print('{0} next run time = {1}'.format(job.func, job.next_run_time))
    print(scheduler.get_jobs())


print(scheduler.get_jobs())
scheduler.start()

print('started scheduler')
