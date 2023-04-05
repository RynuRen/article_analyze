import io
import json
import pandas as pd

import boto3


with open('./config.json', 'r') as f:
    config = json.load(f)


    
class boto_s3:
    # this class may not use
    # now, use "aws 3s"  command to upload data_mecab_category/*.csv 
    aws_access_key_id = config['aws']['aws_access_key_id']
    aws_secret_access_key = config['aws']['aws_secret_access_key']
    bucket = config['s3']['bucket']
    
    def __init__(self):
        self.s3 = boto3.resource('s3',
                                 aws_access_key_id=self.aws_access_key_id,
                                 aws_secret_access_key=self.aws_secret_access_key)
        
    def set_filter(self,directory):
        self.directory =directory
        
    def get_list(self):
        bucket = self.s3.Bucket(self.bucket)
        files = [obj.key for obj in bucket.objects.filter(Prefix=self.directory)]
        return files
        

        
    def get_df(self, file):
        obj = self.s3.Object(self.bucket, file)
        data = obj.get()['Body'].read().decode('utf-8')
        df = pd.read_csv(io.StringIO(data))
        
        return df
    
    def upload_df(self,file_name,df):
        
        csv_buffer = io.StringIO()
        df.to_csv(csv_buffer, index=False)
        csv_buffer.seek(0)
        
        self.s3.Object(self.bucket, file_name).put(Body=csv_buffer.getvalue())
