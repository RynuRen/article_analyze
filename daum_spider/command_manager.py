import json
from base_set import path_manager
from data_refresher import data_refresher
from setting_refresher import setting_refresher

with open('./config_data.json', 'r') as f:
    config_data = json.load(f)

class command_manager:
    
    
    def __init__(self, m , md, c, d, dt):
        # mode, mode_detail, date, date_to
        self.m = m
        self.md = md
        self.c = c
        self.d = d
        self.dt = dt
 
    
    
    def mange_mode(self):

        if self.m in ["data","d","day"] or not self.m:
            if self.md in ["yes","y","Y","dtm"] or not self.md:
                self.data_refresher =data_refresher()
                self.mange_data()
                
            elif self.md in ["no","n","N"]:
                self.data_refresher =data_refresher(dtm_mode = False)
                self.mange_data()
                
            
            
        elif self.m in ["adapt","a"]:
            if self.d or self.dt :
                return
            
               
            if self.md in ["model","m"] or not self.md:
                self.setting_refresher = setting_refresher(change_token = False)
                self.mange_adapt()
            elif self.md in ["token","t"]:
                self.setting_refresher = setting_refresher(change_token = True)
                self.mange_adapt()
                
    
            
    def mange_data(self):
        if self.c :
            if self.c in config_data['data']['category']:
                self.data_action(self.c)
                
            else:
                return
                
        
                
    def data_action(self,category):
        path_manager.set_category(category)
        self.data_refresher.set_category(category)
        if not self.d and self.dt:
            return

        elif not self.d and not self.dt:
            self.data_refresher.get_yesterday()

        elif self.d and not self.dt:
            self.data_refresher.get_day(self.d)

        elif self.d and self.dt:
            self.data_refresher.get_day_from_to(self.d, self.dt)

        
        
    def mange_adapt(self):
        if self.c :
            if self.c in config_data['data']['category']:
                path_manager.set_category(self.c)
                self.setting_refresher.set_category(self.c)
                self.setting_refresher.refresh_model_and_data()
            else:
                return

            
            
            
        

        

    # def re_get_mecav_

if __name__ == "__main__":
    
    import argparse
    main_img="""
     /$$                 /$$                                               /$$             /$$     
    | $$                | $$                                              |__/            | $$    
    | $$   /$$  /$$$$$$ | $$   /$$  /$$$$$$   /$$$$$$        /$$$$$$/$$$$  /$$ /$$$$$$$  /$$$$$$  
    | $$  /$$/ |____  $$| $$  /$$/ |____  $$ /$$__  $$      | $$_  $$_  $$| $$| $$__  $$|_  $$_/  
    | $$$$$$/   /$$$$$$$| $$$$$$/   /$$$$$$$| $$  \ $$      | $$ \ $$ \ $$| $$| $$  \ $$  | $$    
    | $$_  $$  /$$__  $$| $$_  $$  /$$__  $$| $$  | $$      | $$ | $$ | $$| $$| $$  | $$  | $$ /$$
    | $$ \  $$|  $$$$$$$| $$ \  $$|  $$$$$$$|  $$$$$$/      | $$ | $$ | $$| $$| $$  | $$  |  $$$$/
    |__/  \__/ \_______/|__/  \__/ \_______/ \______/       |__/ |__/ |__/|__/|__/  |__/   \___/  \n
                                                                            data, model refresher  \n\n\n
                    %commander , before use mode adapt, must deactive crontab% \n"""
    
    print(main_img)
    parser = argparse.ArgumentParser()
    
    parser.add_argument('-m', type=str, help= 'mode - data(default)/adapt : select mode')
    parser.add_argument('-md', type=str, help= 'mode_detail - make_model? Y(default)/N\n : get_dtm? Y(default)/N')
    parser.add_argument('-c', type=str, help= 'category - target category')
    parser.add_argument('-d', type=str, help = 'date(date_from) for data - target date ')
    parser.add_argument('-dt', type=str, help = 'date_to for data - target date : date from ~ date to' )
                        
    

    args = parser.parse_args()

    
    commander = command_manager( args.m , args.md, args.c, args.d, args.dt)
    print("--running----------------------------------------------------")
    
    commander.mange_mode()
            
    
            
        
            
             
            
            
        
        
        
        

