from apscheduler.schedulers.background import BackgroundScheduler

scheduler = BackgroundScheduler()


def shut_down():
    scheduler.remove_all_jobs()
    scheduler.shutdown()
