from sklearn.metrics.pairwise import cosine_similarity
from tqdm import tqdm
import pandas as pd
from glob import glob
from bs4 import BeautifulSoup
import requests
import pickle
import pymysql as m
from scipy.sparse import csr_matrix
import ast

stop_content = ["무단전재", "재배포금지","저작권자 ⓒ 서울신문사","무단복제 및 전재","무단 전재 및 재배포","제보는 카톡", "☞", "무단 전재-재배포", "▶연합뉴스 앱 지금 바로 다운받기~"]
rep_list = ['기사내용 요약']
except_list = ['Exclamation', 'Josa', 'KoreanParticle', 'Determiner',  'Eomi', 'Suffix',  'VerbPrefix', 'PreEomi']
include_list = ['Verb',  'Noun']

files = sorted(glob('./data/*.csv'))
date_cols = ['date']
data_all = pd.DataFrame()



with open("./model/pickled_tfidf_vec.bin", "rb") as f:
    tfidf_vec = pickle.load(f)
    

def ErrorLog_sql_dtm(error: str, element = None):
    current_time = time.strftime("%Y.%m.%d/%H:%M:%S", time.localtime(time.time()))
    with open("error_Log_sql_dtm.txt", "a") as f:
        f.write(f"[{current_time}] - {error}\n")
        if element != None:
            f.write(f"[{element}\n\n")       



def send_mysql_select_dtm(press,drange):
#    언론사 및 날짜 링크 -> 조인 -> dtm
    
    con = m.connect(host = "localhost", user = 'root', db = 'mintsaveworld' , password = "", charset = "utf8mb4")
    cur = con.cursor()
    
    sql = """SELECT society_dtm.link, data_, indices, indptr
    FROM society
    JOIN society_dtm
    ON society.link = society_dtm.link AND society.press = %s AND society.date BETWEEN (%s) AND (%s);
	"""
        
    cur.execute(sql,(press, drange[0], drange[1]))
    
    result = cur.fetchall()
    
    con.commit()
    con.close()

    return result

def send_mysql_select_dtm_set():
    
    con = m.connect(host = "localhost", user = 'root', db = 'mintsaveworld' , password = "", charset = "utf8mb4")
    cur = con.cursor()
    
    sql = """SELECT shape from society_dtm_set;"""
        
    cur.execute(sql)
    result = cur.fetchall()
    
    con.commit()
    con.close()

    return result[0][0]

def send_mysql_select_content(link_list):
#  링크 -> 필요한 정보
    
    con = m.connect(host = "localhost", user = 'root', db = 'mintsaveworld' , password = "", charset = "utf8mb4")
    cur = con.cursor()
    result = []
    
    for q_link in link_list:
        sql = """select press, title, date, link from society where link = %s;"""

        cur.execute(sql,(q_link))
        result.append(cur.fetchall()[0])
    
    con.commit()
    con.close()

    return result

# 토크나이저 적용
def string_tokenizer(content):
    import pandas as pd
    from konlpy.tag import Okt
    okt = Okt()
    
    content = pd.Series(content)
    # 한글, 숫자, 공백만 남기기
    content = content.str.replace('[^0-9ㄱ-ㅎㅏ-ㅣ가-힣 ]', '', regex=True)
    token_content = okt.pos(content[0], norm=True, stem=True)
    return token_content

def tag_except_list(except_list, token_content):
    filtered_list= []
    for tag in token_content :
        if tag[1] in except_list:
            continue
        filtered_list.append(tag[0])
    content = " ".join(filtered_list)
    return content

# def tag_include_list(include_list, token_content):
#     filtered_list=[]
#     for tag in token_content :
#         if tag[1] in include_list:
#             filtered_list.append(tag[0])

#     content = " ".join(filtered_list)
#     return content

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

def content_return_top(tfidf_content,n,drange, press):
    from sklearn.metrics.pairwise import cosine_similarity
    ###########################################################################################################################################
#     data_filtered_idx = data_all[(data_all['date'].isin(pd.date_range(drange[0], drange[1]))) & (data_all['press']==press)].index

#     tfidf_dtm_filtered = tfidf_dtm[data_filtered_idx]
#     cos_sim_res = cosine_similarity(tfidf_content, tfidf_dtm_filtered)
    ### 수정 계획 ##########################################
    #지정된 언론사, 날짜 내에서 sql select, link-dtm 정보
    #가져온 link-dtm를 리스트 화
    #추천된 링크로 sql select, 필요 정보
    # tag_list = ast.literal_eval(token)
    
    dtm_datas = send_mysql_select_dtm(press, drange)
    shape_ = send_mysql_select_dtm_set()
    
    csr_mat_list = []
    links = []
    
    
    for dtm_data in dtm_datas:
        data_ = ast.literal_eval(dtm_data[1])
        indices = ast.literal_eval(dtm_data[2])
        indptr = ast.literal_eval(dtm_data[3])
        csr_mat_list.append(csr_matrix((data_,indices,indptr), shape = (1,shape_)))
        links.append(dtm_data[0])
        

    
    
    cos_sim_res=[]
    for cst_mat in csr_mat_list:
        cos_sim_res.append(cosine_similarity(tfidf_content, cst_mat)[0][0])


    # 유사도 정렬, 추출
    sim_scores = list(zip(links, cos_sim_res))
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    sim_scores_n = sim_scores[1:n+1]
    # sim_scores_n 은 (기사 링크 리스트, 유사도) 리스트 입니다.
    

    # 해당하는 기사 가져오기
    # article_idx = [article_dict[0] for article_dict in sim_scores_n]
    link_list= [article_dict[0] for article_dict in sim_scores_n]
    
    
    top_n_data = send_mysql_select_content(link_list)
    rst = pd.DataFrame(top_n_data, columns = ['press', 'title', 'date', 'link'])
    
    
    rst['similarity'] = [round(sim[1], 4)*100 for sim in sim_scores_n]

###########################################################################################################################################
    return rst[['press', 'similarity', 'title', 'date', 'link']]
    

    


# 다음 뉴스 url로 추천
def get_recommend_by_url(url, n, drange, press):
    # 다음 뉴스인지 확인
    if not url.startswith('https://v.daum.net/v/'):
        return False
    
    ## 입력 처리
    # 본문 스크랩
    content = content_only_scraper(url)
    # 형태소 전처리
    token = tag_except_list(except_list, string_tokenizer(content))
    # tfidf 연산
    tfidf_content = tfidf_vec.transform([token])
    
    ## 조회 한정
    # 기간 & 언론사
    rst = content_return_top(tfidf_content,n,drange, press)
      
    return rst[['press', 'similarity', 'title', 'date', 'link']]

# 문자열로 추천
def get_recommend_by_content(content, n, drange, press):
    from sklearn.metrics.pairwise import cosine_similarity
    ## 입력 처리
    # 형태소 전처리
    token = tag_except_list(except_list, string_tokenizer(content))
    # tfidf 연산
    tfidf_content = tfidf_vec.transform([token])
    
    ## 조회 한정
    # 기간 & 언론사
    
    rst = content_return_top(tfidf_content,n,drange, press)
    
    
    return rst[['press', 'similarity', 'title', 'date', 'link']]