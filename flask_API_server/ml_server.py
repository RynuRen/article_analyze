from flask import Flask, render_template, request, send_from_directory
import os
from recommand import *

def build_home_page():
    page = f"""
    <html>
    <head>
    </head>
    <body>
    <h1>이전 기사 검색</h1>
    <p>이전 기사 검색 시스템</p>
    <a href="/input">검색하러 가기</a>
    </body>
    </html>
    """
    return page

def build_input_page():
    page = f"""
    <html>
    <head>
    </head>
    <body>
    <form action="/result", method="get">
        <fieldset style="width:500">
            <legend>url로 검색</legend><br>
                다음 뉴스 url 입력<br>
                <textarea type="text" placeholder="https://v.daum.net/v/" name="url" style="width:100%; height:21px; resize:none" required></textarea><br>
                언론사
                <select name="press">
                    <option value="경향신문">경향신문</option>
                    <option value="국민일보">국민일보</option>
                    <option value="뉴스1">뉴스1</option>
                    <option value="뉴시스">뉴시스</option>
                    <option value="동아일보">동아일보</option>
                    <option value="문화일보">문화일보</option>
                    <option value="서울신문">서울신문</option>
                    <option value="세계일보">세계일보</option>
                    <option value="연합뉴스">연합뉴스</option>
                    <option value="조선일보">조선일보</option>
                    <option value="중앙일보">중앙일보</option>
                    <option value="한겨레">한겨레</option>
                    <option value="한국일보">한국일보</option>
                </select><br>
                시작 날짜
                <input type="date" value="2021-01-01" min="2021-01-01" max="2022-12-31" name="startdate" step="1" required></input>
                마지막 날짜
                <input type="date" value="2022-12-31" min="2021-01-01" max="2022-12-31" name="enddate" step="1" required></input><br>
                <input type="submit" value="탐색"/>
                <input type="reset" value="초기화"/><br><br>
        </fieldset>
    </form>
    
    <form action="/result", method="get">
        <fieldset style="width:500">
            <legend>기사 본문으로 검색</legend><br>
            <textarea type="text" placeholder="기사 본문" name="content" style="width:100%; height:200px; resize: none" required></textarea><br>
            언론사
                <select name="press">
                    <option value="경향신문">경향신문</option>
                    <option value="국민일보">국민일보</option>
                    <option value="뉴스1">뉴스1</option>
                    <option value="뉴시스">뉴시스</option>
                    <option value="동아일보">동아일보</option>
                    <option value="문화일보">문화일보</option>
                    <option value="서울신문">서울신문</option>
                    <option value="세계일보">세계일보</option>
                    <option value="연합뉴스">연합뉴스</option>
                    <option value="조선일보">조선일보</option>
                    <option value="중앙일보">중앙일보</option>
                    <option value="한겨레">한겨레</option>
                    <option value="한국일보">한국일보</option>
                </select><br>
                시작 날짜
                <input type="date" value="2021-01-01" min="2021-01-01" max="2022-12-31" name="startdate" step="1" required></input>
                마지막 날짜
                <input type="date" value="2022-12-31" min="2021-01-01" max="2022-12-31" name="enddate" step="1" required></input><br>
                <input type="submit" value="탐색"/>
            <input type="reset" value="초기화"/><br><br>
        </fieldset>
    </form>
    <a href="/">메인으로</a>
    </body>
    </html>
    """
    return page

def build_result_df():
    url = request.args.get("url")
    content = request.args.get("content")
    startdate = request.args.get("startdate")
    enddate = request.args.get("enddate")
    press = request.args.get("press")
    startdate = ''.join(list(startdate.split('-')))
    enddate = ''.join(list(enddate.split('-')))
    startdate, enddate = sorted([startdate, enddate])

    global tfidf_dtm
    global tfidf_vec
    
    if url:
        res_df = get_recommend_by_url(url, 10, (startdate, enddate), press)
    else:
        res_df = get_recommend_by_content(content, 10, (startdate, enddate), press)

    res_df['date-1m'] = res_df['date'] + pd.to_timedelta(-res_df['date'].dt.days_in_month, unit='D')
    res_df['date-1d'] = res_df['date'] + pd.to_timedelta(-1, unit='D')
    res_df['search'] = "/result?url="+res_df['link']+"&press="+press+"&startdate="+res_df['date-1m'].dt.strftime("%Y-%m-%d")+"&enddate="+res_df['date-1d'].dt.strftime("%Y-%m-%d")

    return res_df[['similarity', 'press', 'title', 'date', 'link', 'search']]

app = Flask(__name__)

@app.route("/favicon.ico")
def favicon():
	return send_from_directory(os.path.join(app.root_path, 'static'),'favicon.ico', mimetype='image/vnd.microsoft.icon')

@app.route("/")
def home():
    page = build_home_page()
    return page

@app.route("/input")
def app_input():
    page = build_input_page()
    return page

@app.route("/result", methods=["POST", "GET"])
def app_result():
    res_df = build_result_df()
    # try:
    #     res_df = build_result_df()
    # except:
    #     return render_template("error.html")
    
    return render_template("result.html",
                            column_names=res_df.columns.values,
                            row_data=list(res_df.values.tolist()),
                            link_column="link", zip=zip)

if __name__ == '__main__':
    # app.run(host="192.168.10.48", port=5000)
    app.run(host="localhost", port=5000)
    # app.run(host="192.168.0.54", port=5000)

# TODO
# 형태소 결정, token 컬럼 저장
# DB 연동
# 최대 출력 갯수, 날짜 지정, 정확도 옵션 추가
# 검색 시간 단축 고민