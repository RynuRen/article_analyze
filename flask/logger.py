import time

def ErrorLog_scraping(error: str, defname : str):
    err = error.split('\n')[-2]
    current_time = time.strftime("%Y.%m.%d/%H:%M:%S", time.localtime(time.time()))
    with open("./error_Log_scraping.txt", "a") as f:
        f.write(f"[{current_time}] [{defname}] \n {err}\n")
        
def ErrorLog_sql(error: str, defname : str):
    err = error.split('\n')[-2]
    current_time = time.strftime("%Y.%m.%d/%H:%M:%S", time.localtime(time.time()))
    with open("./error_Log_sql.txt", "a") as f:
        f.write(f"[{current_time}] [{defname}] \n {err}\n")
        
def savelog(log):
    current_time = time.strftime("%Y.%m.%d/%H:%M:%S", time.localtime(time.time()))
    with open("./작업로그.txt", "a") as f:
        f.write(f"[{current_time}]\n{log}\n")
