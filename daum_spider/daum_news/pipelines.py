# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
import os
import json
import re
import pandas as pd
from datetime import datetime


from itemadapter import ItemAdapter
from konlpy.tag import Mecab
from base_set import okt_adapter,path_manager

# import boto_s3




        
        
with open('/home/mint/daum_spider/config_data.json', 'r') as f:
    config_data = json.load(f)
            
    
        
class DaumNewsPipeline:
    
    def __init__(self,category, date):
        self.category = category
        self.date = date
        
        self.cur_date = datetime.strptime(self.date, '%Y%m%d')
        self.df_today = pd.DataFrame(columns = ["press_id","news_title","news_link","token","content"] )

        
        path_manager.set_category(category)
            
    @classmethod
    def from_crawler(cls, crawler):
        return cls(category=crawler.spider.category, date = crawler.spider.date)
    
    
    
    
    
    def process_item(self, item, spider):
         
        content_token = okt_adapter.content_to_token(item["content"])
        
        tmp = pd.DataFrame({"press_id" : item['press_id'],
                            "news_title" : item['title'],
                            "news_link" : item['link'],
                            "token" : content_token,
                            "content":item["content"]},index = [0])
        
        self.df_today = pd.concat([self.df_today, tmp])
        
        return item

    
    
    def close_spider(self, spider):
        self.df_today['news_date'] = self.cur_date

        self.df_today.reset_index(drop=True, inplace = True)
        
                
        df_mecab = self.df_today[["press_id","news_title",'news_date',"news_link","content","token"]]
        directory, filename = path_manager.get_path_data_mecav(self.cur_date)
        path = directory+ filename

        df_mecab.to_csv(path, index=False)

        with open("./log/Log_scraping.txt", "a") as f:
            f.write(f"finished {self.category} {self.date}\n")


            
    
    
    
    
