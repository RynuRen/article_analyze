# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
from konlpy.tag import Mecab
from datetime import datetime
import re
import pickle
import pymysql as pm
import pandas as pd
import os
import db_conn





class DaumNewsPipeline:

    def __init__(self,category, date):
        self.category = category
        self.date = date
        self.cur_date = datetime.strptime(self.date, '%Y%m%d')
        self.db_conn = db_conn.db_conn(self.category, self.cur_date)

        self.df_today = pd.DataFrame(columns = ["news_id", "press_id","news_title","news_link","token"] )


        self.articleid = self.db_conn.get_autoincrement()

        self.mecab = Mecab()

        with open("./model/pickled_tfidf_vec_mecab_nouns_basic_all.bin","rb") as f:
            self.tfidf_vec = pickle.load(f)

        with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"DB last+1 press_id : {self.articleid} \n")
    @classmethod
    def from_crawler(cls, crawler):
        return cls(category=crawler.spider.category, date = crawler.spider.date)



    def process_item(self, item, spider):

        content_tmp = re.sub('[^0-9ㄱ-ㅎㅏ-ㅣ가-힣 ]'," ",item["content"])
        content_tmp_mecab = self.mecab.nouns(content_tmp)


        content_token = " ".join(content_tmp_mecab)

        press_id = item['press_id']


        tmp = pd.DataFrame({"news_id" : self.articleid,
                            "press_id" : press_id,
                            "news_title" : item['title'],
                            "news_link" : item['link'],
                            "token" : content_token},index = [0])

        self.df_today = pd.concat([self.df_today, tmp])
        self.articleid = self.articleid +1

        return item



    def close_spider(self, spider):


        self.db_conn.insert_date()
        self.db_conn.insert_df(self.df_today)
        check =  self.db_conn.get_autoincrement()

        if check != self.articleid:
            self.db_conn.con.close()
            with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"syn failed, must have to retry\n")

        else:

            for press_id in range(1,14):
                df_day_press = self.df_today[self.df_today["press_id"] == press_id]

                if len(df_day_press) == 0:
                    continue;
                test_len = len(list(df_day_press["token"]))
                dtm = self.tfidf_vec.transform(df_day_press["token"])
                id_list = list(df_day_press["news_id"])



                bin_ = [id_list,dtm]

                path = f"./dtm_data/{self.category}/{press_id}/{self.cur_date.year}/{self.cur_date.month}/{self.cur_date.day}.bin"
                directory= f"./dtm_data/{self.category}/{press_id}/{self.cur_date.year}/{self.cur_date.month}"

                if not os.path.exists(directory):
                    os.makedirs(directory)
                with open(path, "wb") as f:
                    pickle.dump(bin_, f)


            df_mecab = self.df_today[["news_id","press_id","token"]]
            df_mecab.to_csv(f'./mecab_data/{self.category}_{self.cur_date.year}{self.cur_date.month}{self.cur_date.day}.csv',index = None)



            self.db_conn.con.commit()
            self.db_conn.con.close()

            with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"finished {self.category} {self.date}, last+1 press_id {self.articleid} DB last press_id +1: {check}\n")


