import json
import paramiko
import os
from tqdm import tqdm
import pickle

from my_annotation import singleton
from base_set import path_manager

with open('./config.json','r') as f:
    config = json.load(f)
    


    


class my_client:
    __host = config['ec2']['flask']['ip']
    __username = config['ec2']['flask']['user_name']
    
    
    def __init__(self):
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        self.client.connect(hostname=self.__host, username=self.__username)
        self.sftp = self.client.open_sftp()
        self.transport = self.client.get_transport()
        
#     def reconnect(self):
#         print(f"self.transport.is_active() {self.transport.is_active()}")
#         if self.transport.is_active():
#             return
            
#         else:
        
        
#             self.client = paramiko.SSHClient()
#             self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
#             self.client.connect(hostname=self.__host, username=self.__username)
#             self.sftp = self.client.open_sftp()
#             self.transport = self.client.get_transport()
        
    def execute_command(self, cmd):
        stdin, stdout, stderr = self.client.exec_command(cmd)
        output = stdout.read()
        error = stderr.read()
        if error:
            with open("./log/Ssh_cmd.txt", "a") as f:
                f.write(f"{error.decode()}\n")
            
            return error.decode()
            
        return output.decode()
    
    
    def mkdir_remote(self,flask_directory):
        cmd = "mkdir -p " + flask_directory
        self.execute_command(cmd)
        
    def rm_directory_remote(self,flask_directory):
        cmd = "rm -r " + flask_directory
        self.execute_command(cmd)
    
    def mv_remote(self,flask_directory_new,flask_directory):
        cmd = "mv " + flask_directory_new + " " + flask_directory
        self.execute_command(cmd)
        
    


    
    
    
    def send_dtm_directory(self):
        
        flask_directory, directory = path_manager.get_directory_dtm_folder_both(new = True)
        
        
        for root, dirs, files in tqdm(os.walk(directory),desc = "send dtm"):
            for filename in files:
                local_path = os.path.join(root, filename)
                remote_path =local_path.replace(directory,flask_directory)
                remote_directory = os.path.dirname(remote_path)
                
                self.mkdir_remote(remote_directory)
                self.sftp.put(local_path, remote_path)

                
    def send_model(self, category):
        flask_directory, directory = path_manager.get_directory_model_both(new = True)
        filename = f"pickled_tfidf_vec_{category}.bin"
        
        local_path = directory + filename
        remote_path = flask_directory +  filename
        
        self.mkdir_remote(flask_directory)
        
        self.sftp.put(local_path, remote_path)
        
                
        
    
    def send_file(self,remote_path,local_path):
        remote_directory= os.path.dirname(remote_path)
        self.mkdir_remote(remote_directory)
        self.sftp.put(local_path,remote_path)
        
        
    def send_day_dtm(self, date_time):
        if isinstance(date_time, str):
            date_time = datetime.strptime(date_time, '%Y%m%d')
            
            
        for press_id in range(1,14):
            flask_directory, directory ,file_name = path_manager.get_path_dtm(press_id, date_time, True)
            path = directory + file_name
            
            if not os.path.exists(path):
                continue
            
            remote_path = flask_directory + file_name
            
            self.send_file(remote_path,path)
    
        
    
    def after_preprocess_setting_adapter(self,filename):
        
        flask_directory_new, _ = path_manager.get_directory_model_both(new = True)
        flask_directory, _ = path_manager.get_directory_model_both(new = False)
        
        path_model_new = flask_directory_new + filename
        path_model = flask_directory + filename
        
        self.mv_remote(path_model_new, path_model)
        self.execute_command(cmd)
        
        
        flask_directory_new, _ = path_manager.get_directory_dtm_folder_both(new = True)
        flask_directory, _ = path_manager.get_directory_dtm_folder_both(new = False)

        self.rm_directory_remote(flask_directory)
        self.mv_remote(flask_directory_new,flask_directory)

        
        
    def close(self):
        self.sftp.close()
        self.client.close()