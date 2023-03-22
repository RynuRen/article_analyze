from logger import ErrorLog_sql
import pymysql as pm
import traceback
import json

with open('./config.json', 'r') as f:
    config = json.load(f)

# DB 연결 정보
class DdCon:
    conf = {
        'host' : config['DB']['host'],
        'user' : config['DB']['user'],
        'pwd' : config['DB']['pwd'],
        'db' : config['DB']['db'],
        'char' : config['DB']['char']
    }
    
    def __init__(self, **kwargs):
        self.dbcon = self.sql_connect()
    
    def sql_connect(self):
        try:
            return pm.connect(**self.conf)
        except pm.Error as err:
            print(err)

    def select_info(self, id_list):
        # 링크 -> 필요한 정보
        con = pm.connect(**self.conf)
        cur = self.dbcon.cursor()
        result = []
        id_list = ",".join(list(map(str, id_list)))
        try:
            sql = f"""SELECT id, press_id, title, date, link FROM society WHERE id IN ({id_list});"""
            cur.execute(sql)
            result = cur.fetchall()
        except:
            ErrorLog_sql(str(traceback.format_exc()),"final_sql : send_mysql_select_content")
        
        self.dbcon.commit()
        # con.close()

        return result