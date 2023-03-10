from logger import ErrorLog_scraping
import aws_boto3

stop_content = ["무단전재", "재배포금지","저작권자 ⓒ 서울신문사","무단복제 및 전재","무단 전재 및 재배포","제보는 카톡", "☞", "무단 전재-재배포", "▶연합뉴스 앱 지금 바로 다운받기~"]
except_list = ['Exclamation', 'Josa', 'KoreanParticle', 'Determiner',  'Eomi', 'Suffix',  'VerbPrefix', 'PreEomi']
rep_list = ['기사내용 요약']
tfidf_vec = aws_boto3.open_s3("model/pickled_tfidf_vec.bin")

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
                return False
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
def content_scraper(link):
    from datetime import datetime
    # 다음 뉴스인지 확인
    if not link.startswith('https://v.daum.net/v/'):
        return False
    article_soup = create_soup(link)

    # 제목 얻기
    title = article_soup.find('h3', attrs={'class':'tit_view'}).text
    
    # 날짜 얻기
    date = article_soup.find('span', attrs={'class':'num_date'}).text
    date = datetime.strptime(date, '%Y. %m. %d. %H:%M').strftime('%Y-%m-%d')

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
    return title, date, content

def content2dtm(content):
    # 형태소 전처리
    token = string_tokenizer(content)

    # tfidf 연산
    tfidf_content = tfidf_vec.transform([token])
    return tfidf_content