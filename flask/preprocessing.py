from logger import ErrorLog_scraping
import dataload

class ContentTooShort(Exception):
    pass

class ConnectLinkError(Exception):
    pass

stop_content = ["SBS Biz에 제보", "네이버에서 SBS Biz", "당신의 제보가 뉴스로 만들어집니다.", "무단전재", "재배포금지","저작권자 ⓒ 서울신문사","무단복제 및 전재","무단 전재 및 재배포","제보는 카톡", "☞", "무단 전재-재배포", "▶연합뉴스 앱 지금 바로 다운받기~"]
except_list = ['Exclamation', 'Josa', 'KoreanParticle', 'Determiner',  'Eomi', 'Suffix',  'VerbPrefix', 'PreEomi']
rep_list = ['기사내용 요약']
tfidf_vec = dataload.open_pickle("model/pickled_tfidf_vec_society.bin")

# soup 생성
def create_soup(url):
    from bs4 import BeautifulSoup
    import requests
    import traceback

    i = 0
    # 요청 오류시 10번  재시도
    while i < 10 :
        try:
            res = requests.get(url)
            res.raise_for_status()
            soup = BeautifulSoup(res.content, 'html.parser', from_encoding='cp949')
            break
        except:
            i += 1
            if i == 10 :
                ErrorLog_scraping(str(traceback.format_exc()),"final_preprocessing : create_soup")
                raise ConnectLinkError
    return soup

# 토크나이저 적용
def string_tokenizer(content):
    import pandas as pd
    from konlpy.tag import Okt
    okt = Okt()
    
    content = pd.Series(content)
    # 한글, 숫자, 공백만 남기기
    content = content.str.replace('[^0-9ㄱ-ㅎㅏ-ㅣ가-힣 ]', '', regex=True)
    token_content = okt.pos(content[0], norm=True, stem=True)
    filtered = []
    for token in token_content:
        if token[1] in except_list:
            continue
        filtered.append(token[0])
    filtered_str = ' '.join(filtered)
    return filtered_str

# link의 기사 본문 추출
def cur_news_scraper(link):
    # 다음 뉴스인지 확인
    if link.startswith('https://v.daum.net/v/'):
        return daumnews_scraper(link)
    else:
        return newspaper_scraper(link)
    
def daumnews_scraper(link):
    from datetime import datetime
    article_soup = create_soup(link)
    cur_news = {}
    cur_news['newsLink'] = link

    # 제목 얻기
    title = article_soup.find('h3', attrs={'class':'tit_view'}).text
    title = title.replace('"', '\"')
    cur_news['newsTitle'] = title
    
    # 날짜 얻기
    date = article_soup.find('span', attrs={'class':'num_date'}).text
    date = datetime.strptime(date, '%Y. %m. %d. %H:%M').strftime('%Y-%m-%d')
    cur_news['newsDate'] = date

    # 언론사 얻기
    press = article_soup.find('a', attrs={'data-tiara':'언론사명'}).text
    cur_news['newsPress'] = press

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
        raise ContentTooShort()
    
    return cur_news, content

def newspaper_scraper(link):
    import requests
    from newspaper import Article
    r = requests.get(link)
    link = r.url
    article = Article(link, language="ko")
    article.download()
    article.parse()
    cur_news = {}
    cur_news['newsLink'] = link

    # 제목 얻기
    cur_news["newsTitle"] = article.title
    
    # 날짜 얻기
    date = article.publish_date.strftime('%Y-%m-%d')
    cur_news['newsDate'] = date
    
    # 언론사 얻기
    press = article.meta_data['og']['site_name']
    cur_news['newsPress'] = press
    
    # 본문 정리
    content = article.text
    rst = []
    for tmp in content.split('\n'):
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
        raise ContentTooShort()
    
    return cur_news, content

def content2dtm(content):
    # 형태소 전처리
    token = string_tokenizer(content)

    # tfidf 연산
    tfidf_content = tfidf_vec.transform([token])
    return tfidf_content