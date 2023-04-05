

from datetime import datetime, timedelta
from dateutil.rrule import rrule, DAILY
from tqdm import tqdm
import pickle
from glob import glob
import pandas as pd

from scrapy import crawler
from multiprocessing import Process, Queue
from twisted.internet import reactor
from scrapy.crawler import CrawlerProcess
from scrapy.utils.project import get_project_settings
from daum_news.spiders.daum import DaumSpider as MySpider

from daum_news.spiders.daum import f



from DB_controller import DB_con
from base_set import path_manager, okt_adapter
from my_client import my_client

            
def run_spider(spider,category,datelist,num_spider):
    
    q = Queue()
    p = Process(target=f, args=(q,spider,category,datelist,num_spider))
    p.start()
    result = q.get()
    p.join()

    if result is not None:
        raise result

class data_refresher:
    def __init__(self, category=None, dtm_mode = True):
        # dtm_mode T: model is alreadly exist
        # dtm_mode F: model is not exist, get news_data for fit model
        self.category = category
        self.dtm_mode = dtm_mode
        self.DB_con = DB_con(category)
        settings=get_project_settings()
        settings['CONCURRENT_ITEMS'] = 10
        self.spider_execute = 0;
        self.datelist=[]

    def set_category(self,category):
    
        self.category = category
        self.DB_con.set_category(category)
        
    def set_date(self,date):
        self.date = date
        
    
    def set_date_to(self,date):
        self.date_to = date_to

            
    

     
           
    def scapy_date(self,date):
        if isinstance(date, datetime):
            date = date.strftime('%Y%m%d')

        self.datelist.append(date)
        self.spider_execute = self.spider_execute + 1
        if self.spider_execute == 10:

            run_spider(MySpider,self.category,self.datelist,self.spider_execute)
            self.spider_execute = 0
            self.datelist = []

    def start_scape(self):
        if len(self.datelist) != 0:
            run_spider(MySpider,self.category,self.datelist,self.spider_execute)
            self.spider_execute = 0
            self.datelist = []
        

        
    def get_yesterday(self):
        now = datetime.now()
        delta = timedelta(days=1)
        yesterday = now - delta
        db_most_resently_date = self.DB_con.select_max_date()
        
        if yesterday.date() != db_most_resently_date: 
            date = yesterday.strftime("%Y%m%d")
            
            self.scapy_date(date)
            self.start_scape()
            self.DB_upload_data(date)


    def get_day(self, date):
        db_date_list = self.DB_con.get_distinct_date()
        targetdate = datetime.strptime(date, '%Y%m%d')
        
        if not targetdate.date() in db_date_list:

            self.scapy_date(date)
            self.start_scape()
            self.DB_upload_data(date)

    

    
    
    def set_autoincrement(self):
        self.articleid = self.DB_con.get_autoincrement()
        
        
    def set_tfidf_vec(self):
        with open(f"./model/pickled_tfidf_vec_{self.category}.bin","rb") as f:
                    self.tfidf_vec = pickle.load(f)

        
    # method for DataFrame apply
    def increse_articleid(self,_):
        tmp = self.articleid
        self.articleid = self.articleid +1
        return tmp
            
    def get_day_from_to(self, date, date_to):
        db_date_list = self.DB_con.get_distinct_date()
        
        date_time_from = datetime.strptime(date, '%Y%m%d')
        date_time_to = datetime.strptime(date_to, '%Y%m%d')
        
        for date_time_dt in list(rrule(DAILY, dtstart=date_time_from, until=date_time_to)):
            if date_time_dt.date() in db_date_list:
                continue
            
            date = date_time_dt.strftime("%Y%m%d")
            
            self.scapy_date(date)
           
        self.start_scape()
        self.DB_upload_data(date_time_from, date_time_to, db_date_list)
        
        
                 

    def DB_upload_data(self,date_time_from, date_time_to = None , db_date_list = None):
        
        self.DB_con.con.ping(reconnect=True)

        if not date_time_to:
            date_time_to = date_time_from

        if isinstance(date_time_from, str):
            date_time_from = datetime.strptime(date_time_from, '%Y%m%d')
        if isinstance(date_time_to, str):
            date_time_to = datetime.strptime(date_time_to, '%Y%m%d')



        self.set_autoincrement()

        if self.dtm_mode:
            self.set_tfidf_vec()


        for date_time_dt in tqdm(list(rrule(DAILY, dtstart=date_time_from, until=date_time_to)),desc="upload to DB,(with dtm_mode) create dtm"):
            
            if db_date_list and date_time_dt.date() in db_date_list:
                continue

            directory, filename = path_manager.get_path_data_mecav(date_time_dt)
            mecab_path = directory+ filename
            df_today= pd.read_csv(mecab_path)

            self.DB_con.insert_date(date_time_dt)

            for press_id in range(1,14):
                
                df_today_press = df_today[df_today["press_id"] == press_id]
                
                if len(df_today_press) == 0:
                    continue;
                    
                df_today.loc[df_today_press.index,"news_id"]= df_today_press["press_id"].apply(self.increse_articleid)


                df_today_press = df_today[df_today["press_id"] == press_id]
                self.DB_con.insert_df(df_today_press)

                check = self.DB_con.cur.rowcount

                if check != len(df_today_press):
                    self.DB_con.con.rollback()
                    with open("./log/Log_scraping.txt", "a") as f:
                        f.write(f"syn failed, must have to retry\n")

                    return
                
                if self.dtm_mode:

                    dtm = self.tfidf_vec.transform(df_today_press["token"])
                    id_list = list(df_today_press["news_id"])
                    bin_ = [id_list,dtm]

                    directory, file_name = path_manager.get_path_dtm(press_id,date_time_dt)
                    path = directory + file_name

                    with open(path, "wb") as f:
                        pickle.dump(bin_, f)

            df_mecab = df_today[["news_id","press_id","content","token"]]
            df_mecab.to_csv(mecab_path, index = False )
            
            self.DB_con.con.commit()

               
        if self.dtm_mode:
            my_cl = my_client()
            for date_time_dt in tqdm(list(rrule(DAILY, dtstart=date_time_from, until=date_time_to)),desc="send dtm"):

                if db_date_list and date_time_dt.date() in db_date_list:
                    continue
                my_cl.send_day_dtm(date_time_dt)
            
        
        
    
    
    

    
