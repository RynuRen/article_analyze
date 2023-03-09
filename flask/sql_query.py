from logger import ErrorLog_sql
import pymysql as pm
import traceback
import json

with open('./config.json', 'r') as f:
    config = json.load(f)

# DB 연결 정보
class db_con:
    host = config['DB']['host']
    user = config['DB']['user']
    pwd = config['DB']['pwd']
    db = config['DB']['db']
    char = config['DB']['char']

def send_mysql_select_content(id_list):
    # 링크 -> 필요한 정보
    con = pm.connect(host=db_con.host, user=db_con.user, db=db_con.db, password=db_con.pwd, charset=db_con.char)
    cur = con.cursor()
    result = []
    id_list = ",".join(list(map(str, id_list)))
    try:
        sql = f"""SELECT id, press_id, title, date, link FROM society WHERE id IN ({id_list});"""
        cur.execute(sql)
        result = cur.fetchall()
    except:
        ErrorLog_sql(str(traceback.format_exc()),"final_sql : send_mysql_select_content")
    
    con.commit()
    con.close()

    return result