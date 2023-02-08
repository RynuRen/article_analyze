from sklearn.metrics.pairwise import cosine_similarity
from tqdm import tqdm
import pandas as pd
from glob import glob
from bs4 import BeautifulSoup
import requests
import pickle

stop_content = ["무단전재", "재배포금지","저작권자 ⓒ 서울신문사","무단복제 및 전재","무단 전재 및 재배포","제보는 카톡", "☞", "무단 전재-재배포", "▶연합뉴스 앱 지금 바로 다운받기~"]
rep_list = ['기사내용 요약']

files = sorted(glob('./data/*.csv'))
date_cols = ['date']
data_all = pd.DataFrame()
for file in tqdm(files):
    df_day = pd.read_csv(file, parse_dates=date_cols) # date 컬럼 datetime 형식으로 불러오기
    data_all = pd.concat([data_all, df_day])
data_all.reset_index(drop=True, inplace=True)

with open("./model/pickled_tfidf_dtm.bin", "rb") as f:
    tfidf_dtm = pickle.load(f)
with open("./model/pickled_tfidf_vec.bin", "rb") as f:
    tfidf_vec = pickle.load(f)

# soup 생성
def create_soup(url):
    i = 0
    ## 요청 오류시 10번  재시도
    while i < 10 :
        try:
            res = requests.get(url)
            res.raise_for_status()
            soup = BeautifulSoup(res.content, 'html.parser', from_encoding='cp949')
            break
        except:
            i += 1
            
    return soup

# link의 기사 본문 추출
def content_only_scraper(link):
    article_soup = create_soup(link)

    # 본문 정리
    article = article_soup.find_all('section')[1]
    content = article.find_all(True, attrs={'dmcf-ptype':'general'})
    rst = []
    for para in content:
        for tmp in para.text.split('\n'):
            if tmp.strip() != '':
                rst.append(tmp.strip())
    content = []
    for c in rst:
        for i in stop_content:
            if i in c:
                break
        else:
            for rep in rep_list:
                c = c.replace(rep, '')
            content.append(c)
    content = ' '.join(content)
    if len(content) < 200:
        return False
    
    return content

# 타이틀로 추천
def get_recommend_by_title(title, n, drange=False):
    # 타이틀로 저장된 토큰 호출, 축적된 기사들과 유사도 연산
    token = data_all.loc[data_all['title'] == title]['okt'].values[0]
    tfidf_content = tfidf_vec.transform([token])
    
    # 기간 설정
    if drange:
        data_filtered_idx = data_all[data_all['date'].isin(pd.date_range(drange[0], drange[1]))].index
        tfidf_dtm_filtered = tfidf_dtm[data_filtered_idx]
        cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm_filtered)
    else:
        cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm)
        
    # 유사도 정렬, 추출
    sim_scores = list(enumerate(cos_sim_res[0]))
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    sim_scores_n = sim_scores[1:n+1]
    # sim_scores_n = [sim for sim in sim_scores_n if sim[1] >= 0.6] # 유사도 60% 이상 필터=>옵션화 가능?###############
    
    # 해당하는 기사 가져오기
    article_idx = [article_dict[0] for article_dict in sim_scores_n]
    rst = data_all.iloc[article_idx][['press', 'title', 'date', 'link']]
    rst['similarity'] = [round(sim[1], 4)*100 for sim in sim_scores_n]
        
    return rst[['press', 'similarity', 'title', 'date', 'link']]

# 다음 뉴스 url로 추천
def get_recommend_by_url(url, n, drange=False):
    # 다음 뉴스인지 확인
    if not url.startswith('https://v.daum.net/v/'):
        return False
    # 본문 스크랩
    content = content_only_scraper(url)

    #### 형태소 분석코드 ####
    token = content
    
    tfidf_content = tfidf_vec.transform([token])
    
    # 기간 설정
    if drange:
        data_filtered_idx = data_all[data_all['date'].isin(pd.date_range(drange[0], drange[1]))].index
        tfidf_dtm_filtered = tfidf_dtm[data_filtered_idx]
        cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm_filtered)
    else:
        cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm)
    
    
    # 유사도 정렬, 추출
    sim_scores = list(enumerate(cos_sim_res[0]))
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    sim_scores_n = sim_scores[1:n+1]
    # sim_scores_n = [sim for sim in sim_scores_n if sim[1] >= 0.6] # 유사도 60% 이상 필터=>옵션화 가능?###############
    
    # 해당하는 기사 가져오기
    article_idx = [article_dict[0] for article_dict in sim_scores_n]
    rst = data_all.iloc[article_idx][['press', 'title', 'date', 'link']]
    rst['similarity'] = [round(sim[1], 4)*100 for sim in sim_scores_n]
        
    return rst[['press', 'similarity', 'title', 'date', 'link']]

# 문자열로 추천
def get_recommend_by_content(content, n, drange=False):
    #### 형태소 분석코드 ####
    token = content
    
    tfidf_content = tfidf_vec.transform([token])
    
    # 기간 설정
    if drange:
        data_filtered_idx = data_all[data_all['date'].isin(pd.date_range(drange[0], drange[1]))].index
        tfidf_dtm_filtered = tfidf_dtm[data_filtered_idx]
        cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm_filtered)
    else:
        cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm)
    
    
    # 유사도 정렬, 추출
    sim_scores = list(enumerate(cos_sim_res[0]))
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    sim_scores_n = sim_scores[1:n+1]
    # sim_scores_n = [sim for sim in sim_scores_n if sim[1] >= 0.6] # 유사도 60% 이상 필터=>옵션화 가능?###############
    
    # 해당하는 기사 가져오기
    article_idx = [article_dict[0] for article_dict in sim_scores_n]
    rst = data_all.iloc[article_idx][['press', 'title', 'date', 'link']]
    rst['similarity'] = [round(sim[1], 4)*100 for sim in sim_scores_n]
        
    return rst[['press', 'similarity', 'title', 'date', 'link']]