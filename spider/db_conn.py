import pymysql as pm
import json

with open('./config.json', 'r') as f:
    config = json.load(f)


class db_conn:
    host = config['DB']['host']
    user = config['DB']['user']
    pwd = config['DB']['pwd']
    db = config['DB']['db']
    char = config['DB']['char']

    def __init__(self, category, cur_date = None):
        self.con = pm.connect(host = self.host,
                              user = self.user,
                              db = self.db,
                              password = self.pwd,
                              charset = self.char)
        self.cur = self.con.cursor()
        self.category = category
        self.cur_date = cur_date

    def get_distinct_date(self):
        sql = f"""SELECT date FROM {self.category}_date;"""

        self.cur.execute(sql)
        result = self.cur.fetchall()

        return result

    def select_max_date(self):
        sql = f"""SELECT MAX(date) FROM {self.category}_date;"""
        self.cur.execute(sql)
        result = self.cur.fetchall()[0][0]

        return result


    def get_autoincrement(self):
        sql = f"""SELECT AUTO_INCREMENT FROM information_schema.tables
        WHERE table_name = '{self.category}' AND table_schema = DATABASE( ) ;"""

        self.cur.execute(sql)
        articleid = self.cur.fetchall()[0][0]
        return articleid


    def insert_date(self):
        sql = f"""insert into {self.category}_date VALUES (%s);"""
        self.cur.execute(sql,(self.cur_date))

    def insert_df(self,df):
        df['news_date'] = self.cur_date

        press_id_list = list(df['press_id'])
        news_title_list = list(df['news_title'])
        news_date_list = list(df['news_date'])
        news_link_list = list(df['news_link'])

        q_list = zip(press_id_list,news_title_list,news_date_list,news_link_list)

        sql = f"""insert into {self.category} (press_id, news_title, news_date, news_link) VALUES (%s,%s,%s,%s); """
        self.cur.executemany(sql,q_list)