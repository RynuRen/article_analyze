import warnings
warnings.filterwarnings( 'ignore' )


import json
import os
import shutil
import pickle
import pandas as pd
from datetime import datetime
from glob import glob
from tqdm import tqdm
import subprocess


from sklearn.feature_extraction.text import TfidfVectorizer


from base_set import path_manager ,okt_adapter
from my_client import my_client



class setting_refresher():
    def __init__(self, category=None, change_token = False):
        # dtm_mode T: model is alreadly exist
        # dtm_mode F: model is not exist, get news_data for fit model
        self.category = category
        self.change_token = change_token
        self.my_client = my_client()

    def set_category(self,category):
        self.category = category
        
        
    def file_to_file_adp(self, file):
        
        date= file.split("_")[-1].split(".")[0]
        cur_date = datetime.strptime(date, '%Y%m%d')

        df_today = pd.read_csv(file,usecols=["news_id","press_id","content"])
        df_today['token'] = df_today["content"].apply(okt_adapter.content_to_token)

        
        for press_id in range(1,14):
            df_today_press = df_today[df_today["press_id"]== press_id]

            df_mecab = df_today[["news_id","press_id","content","token"]]
            
            
            directory, filename = path_manager.get_path_data_mecav(date)
            df_mecab.to_csv(directory + filename)
    
    
    def directory_to_directory_adp(self):
        
        
        directory, _ = path_manager.get_path_data_mecav()
        directory_back = directory[:-1] + "_back/"
        
        glob_path = directory_back + "*.csv"
        
        if not os.path.exists(directory_back):
            os.rename(directory, directory_back)
            
        files = glob(glob_path)
        
        files.sort()
        
        path_already = directory + "/*.csv"
        files_already = glob(path_already)
        start_index = 0
        
        if files_already :
            files_ = files_already.sort()[-1].split("/")[-1]
            
            while files[start_index].split("/")[-1] != files_:
                start_index = start_index + 1
                

        for idx in tqdm(range(start_index, len(files)),desc="setting adjust"):
            self.file_to_file_adp(files[idx])
            
        
        shutil.rmtree(directory_back)
    
    
    def refresh_model(self):
        directory, _ = path_manager.get_path_data_mecav()
        glob_path = directory + "*.csv"
        
        files = glob(glob_path)
        token_df = pd.DataFrame(columns = ["token"])
        
        for file in tqdm(files,desc="data load to fit"):
            tmp_df =pd.read_csv(file, usecols=["token"])
            token_df = pd.concat([token_df,tmp_df])
        
        tfidf_vec = TfidfVectorizer()
        tfidf_vec.fit(token_df['token'])
        
        with open(f"model/pickled_tfidf_vec_{self.category}.bin", "wb") as f:
            pickle.dump(tfidf_vec, f)
            
        return tfidf_vec
            
    def refresh_dtm(self,tfidf_vec):
        directory, _ = path_manager.get_path_data_mecav()
        glob_path = directory + "*.csv"
        
        files = glob(glob_path)
        
        for file in tqdm(files,desc="refresh dtm"):
            date = file.split("_")[-1].split(".")[0]
            df_today = pd.read_csv(file, usecols=["news_id","press_id","token"])


            for press_id in range(1,14):
                df_today_press = df_today[df_today["press_id"] == press_id]
                if len(df_today_press) == 0:
                    continue;

                dtm = tfidf_vec.transform(df_today_press["token"])
                id_list = list(df_today_press["news_id"])

                bin_ = [id_list,dtm]


                directory, file_name = path_manager.get_path_dtm(press_id,date)
                path = directory + file_name

                with open(path, "wb") as f:
                    pickle.dump(bin_, f)

    
    
    def refresh_model_and_data(self):
        
        
        if self.change_token:
            self.directory_to_directory_adp()
        
        tfidf_vec = self.refresh_model()
        self.refresh_dtm(tfidf_vec)

        model_file = f"pickled_tfidf_vec_{self.category}.bin"
        
        self.my_client.send_dtm_directory()
        self.my_client.send_model(self.category)
        self.my_client.after_preprocess_setting_adapter(model_file)
    
        
        
        
        
        
        
        
        
            

    
    
    
    

    


            
    
    


    

    
    


    
