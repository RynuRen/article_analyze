from preprocessing import content2dtm
import dataload

press_dict = {'경향신문':1, '국민일보':2, '뉴스1':3, '뉴시스':4, '동아일보':5, '문화일보':6, '서울신문':7, '세계일보':8, '연합뉴스':9,  '조선일보':10, '중앙일보':11, '한겨레':12, '한국일보':13}

def filepathmaker(category, press, drange):
    from dateutil.rrule import rrule, DAILY
    from datetime import datetime
    
    start_date = datetime.strptime(drange[0], '%Y%m%d')
    end_date = datetime.strptime(drange[1], '%Y%m%d')

    path_list = []
    for date_dt in list(rrule(DAILY, dtstart=start_date, until=end_date)):
        path = f"dtm_data/{category}/{press_dict[press]}/{date_dt.year}/{date_dt.month}/{date_dt.day}.bin"
        path_list.append(path)
    
    return path_list

def content_return_top(tfidf_content, n, drange, press):
    import torch
    import pandas as pd
    import numpy as np
    import time

    ## 조회 한정
    # 기간 & 언론사
    start = time.time()
    path_list = filepathmaker("society", press, drange)
    print(f"make path: {time.time() - start}")

    start = time.time()
    id_list, cos_sim_res = dataload.read_parallel(tfidf_content, path_list)
    print(f"load + calc: {time.time() - start}")

    ## 데이터 준비
    # 유사도 정렬, 추출
    if n > len(id_list):
        n = len(id_list)
    sim_scores_n = torch.topk(torch.Tensor(cos_sim_res), k=n)

    # 해당하는 기사 가져오기
    id_list = pd.to_numeric(id_list, downcast='integer')
    top_id_list = id_list[sim_scores_n[1]]
    if type(top_id_list) != np.ndarray: # 검색 결과가 하나일 경우 처리
        top_id_list = [top_id_list]
    else:
        top_id_list = top_id_list.tolist()

    sim_dict = {int(k):float(v) for k, v in zip(top_id_list, sim_scores_n[0].detach().numpy())}
    return sim_dict

# 다음 뉴스 url로 추천
def get_fst_recommend(content, n, drange, press):
    tfidf_content = content2dtm(content)
    rst = content_return_top(tfidf_content, n, drange, press)
    return rst

def get_recommend(news_id, n, drange, tpress):
    from datetime import datetime, timedelta
    category = "society"
    date_dt = datetime.strptime(drange[1], '%Y%m%d') + timedelta(days=1)
    path = f"dtm_data/{category}/{press_dict[tpress[0]]}/{date_dt.year}/{date_dt.month}/{date_dt.day}.bin"
    id_list, dtm_list = dataload.open_pickle(path)
    tfidf_content = dtm_list[int(news_id) - int(id_list[0])]
    rst = content_return_top(tfidf_content, n, drange, tpress[1])
    return rst