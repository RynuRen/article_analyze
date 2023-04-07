import json
import os
import re
import subprocess
import pickle
from datetime import datetime
from konlpy.tag import Mecab



with open('./config.json', 'r') as f:
    config = json.load(f)
    
    

with open('./config_data.json', 'r') as f:
    config_data = json.load(f)


    
class path_manager:
    __flask_directory = config["ec2"]["flask"]["directory"]
    __spider_directory = config["ec2"]["spider"]["directory"]
    category = "test"
    
    
    @classmethod
    def set_category(cls, category):
        cls.category = category
        
    @classmethod
    def mkdir(cls,directory):
        if not os.path.exists(directory):
            os.makedirs(directory)
    
    @classmethod
    def get_path_data_mecav(cls,date=None):
        
        if not date:
            date = 20000101
        if isinstance(date, datetime):
            date = date.strftime('%Y%m%d')
            
        directory = cls.__spider_directory+ f"data_mecab_{cls.category}/"
        cls.mkdir(directory)
        filename = f"{cls.category}_{date}.csv"
        
        return directory, filename
    
    
    @classmethod
    def get_flask_directory(cls,directory_in):
        flask_directory =  cls.__flask_directory + directory_in
        
        return flask_directory
    
    @classmethod
    def get_directory(cls,directory_in):
        directory =  cls.__spider_directory + directory_in
        cls.mkdir(directory)
        
        return directory
    
    
    @classmethod
    def get_path_at_dtm(cls,press_id,date_time):
        
        if isinstance(date_time, str):
            date_time = datetime.strptime(date_time, '%Y%m%d')
        
        directory_in_dtm = f"dtm_data/{cls.category}/{press_id}/{date_time.year}/{date_time.month}/"
        file_name = f"{date_time.day}.bin"
        
        return directory_in_dtm, file_name

    @classmethod
    def get_path_dtm(cls,press_id,date_time,both = None):
        directory_at_dtm, file_name = cls.get_path_at_dtm(press_id,date_time)
        directory= cls.get_directory(directory_at_dtm)
        
        if not both :
            return directory, file_name
        
        flask_directory =  cls.get_flask_directory(directory_at_dtm)
        return flask_directory, directory, file_name
            

    @classmethod
    def get_directory_dtm_folder_both(cls,new = True):
        get_flask_directory(cls,directory_in)
        
        if new:
            flask_directory_in = "dtm_data_new/" + cls.category + "/"
        else:
            flask_directory_in = "dtm_data/" + cls.category + "/"
        
        flask_directory = get_flask_directory(flask_directory_in)
        
        directory_in =  "dtm_data/" + cls.category + "/"
        directory = get_directory(directory_in)
        
        return flask_directory, directory
    
    @classmethod
    def get_directory_model_both(cls,new = True):
        if new:
            flask_directory_in = "model_new/"
        else:
            flask_directory_in = "model/"
        
        flask_directory = flask_directory = get_flask_directory(flask_directory_in)
        directory_in =  "model/"
        
        directory = get_directory(directory_in)
        
        return flask_directory, directory
        
        
        
    
        
class okt_adapter:
    mecab = Mecab()
    tag_list = config_data['preprocess']['tag_list'] 
    regax = config_data['preprocess']['regax'] 
    
    @classmethod
    def content_to_token(cls,content):
        # using in pipelines.py

        content_tmp = re.sub(cls.regax," ",content)
        
        content_tmp_mecab = cls.mecab.pos(content_tmp)
        
        inclu_list = []
        for token in content_tmp_mecab:
            if token[1] in cls.tag_list:
                inclu_list.append(token[0])
        
        content_token = " ".join(inclu_list)
        if len(content_token) == 0:
            content_token = " "

        return content_token
        
        
        

            
            
            
                
    
    
        
        

        
        
        
        
        
    