import argparse
from datetime import datetime, timedelta
import subprocess
import os
from tqdm import tqdm
from dateutil.rrule import rrule, DAILY
import pymysql as pm
import db_conn


parser = argparse.ArgumentParser()
parser.add_argument('arg0', type=str)
parser.add_argument('arg1', type=str)
parser.add_argument('arg2', type=str)
args = parser.parse_args()



category = args.arg0
date_from = datetime.strptime(args.arg1, '%Y%m%d')
date_to = datetime.strptime(args.arg2, '%Y%m%d')



conn = db_conn.db_conn(category)
date_list = conn.get_distinct_date()
date_exist_list = list(zip(*date_list))[0]




for date_dt in tqdm(list(rrule(DAILY, dtstart=date_from, until=date_to))):
    date_dt = date_dt.date()
    if date_dt in date_exist_list:
        continue


    date = date_dt.strftime("%Y%m%d")
    cmd = ["scrapy", "crawl", "daum", "-a", f"category={category}", "-a", f"date={date}"]
    subprocess.call(cmd)

    for press_id in range(1,14):
        path_from = f"""/home/ec2-user/daum_spider/dtm_data/{category}/{press_id}/{date_dt.year}/{date_dt.month}/{date_dt.day}.bin"""

        if not os.path.exists(path_from):
            continue

        mkdir_path = f"/home/ec2-user/dtm_data/{category}/{press_id}/{date_dt.year}/{date_dt.month}/"
        path_to = f""":/home/ec2-user/dtm_data/{category}/{press_id}/{date_dt.year}/{date_dt.month}/"""

        cmd = ["ssh","","mkdir", "-p",mkdir_path]
        subprocess.call(cmd)

        cmd = ["scp",path_from,path_to]
        subprocess.call(cmd)