from datetime import datetime, timedelta
import pymysql as pm
import argparse
import subprocess
import os
import db_conn



parser = argparse.ArgumentParser()
parser.add_argument('arg0', type=str)
args = parser.parse_args()
category = args.arg0

conn = db_conn.db_conn(category)
result= conn.select_max_date()


now = datetime.now()
delta = timedelta(days=1)
yesterday = now - delta



if yesterday.date() != result:
    date = yesterday.strftime("%Y%m%d")

    cmd = ["scrapy", "crawl", "daum", "-a", f"category={category}", "-a", f"date={date}"]
    subprocess.call(cmd)

    for press_id in range(1,14):
        path_from = f"""/home/ec2-user/daum_spider/dtm_data/{category}/{press_id}/{yesterday.year}/{yesterday.month}/{yesterday.day}.bin"""
        if not os.path.exists(path_from):
            continue

        press_id = str(press_id)

        mkdir_path = f"/home/ec2-user/dtm_data/{category}/{press_id}/{yesterday.year}/{yesterday.month}/"
        path_to = f""":/home/ec2-user/dtm_data/{category}/{press_id}/{yesterday.year}/{yesterday.month}/"""
        print(mkdir_path)
        cmd = ["ssh","","mkdir","-p",mkdir_path]
        subprocess.call(cmd)
        cmd = ["scp",path_from,path_to]
        subprocess.call(cmd)