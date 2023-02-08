from flask import Flask, render_template, request
from recommand import *

def build_home_page():
    page = f"""
    <html>
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
    <body>
    제목으로 검색<br>
    <form action="/result", method="get">
    <textarea type="text" placeholder="기사 제목" name="title" style="width:50%; height:21px"></textarea><br>
    <input type="submit"></input><br>
    </form>
    다음 뉴스 url로 검색<br>
    <form action="/result", method="get">
    <textarea type="text" placeholder="다음 뉴스 url" name="url" style="width:50%; height:21px"></textarea><br>
    <input type="submit"></input><br>
    </form>
    기사 본문으로 검색<br>
    <form action="/result", method="get">
    <textarea type="text" placeholder="기사 본문" name="content" style="width:50%; height:200px"></textarea><br>
    <input type="submit"></input><br>
    </form>
    
    </body>
    </html>
    """
    return page

def build_result_df():
    title = request.args.get("title")
    url = request.args.get("url")
    content = request.args.get("content")
    
    global tfidf_dtm
    global tfidf_vec
    
    if title:
        res_df = get_recommend_by_title(title, 10)
    elif url:
        res_df = get_recommend_by_url(url, 10)
    else:
        res_df = get_recommend_by_content(content, 10)
    
    return res_df

app = Flask(__name__)

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
    # return render_template("result.html", tables=[res_df.to_html(classes='data')])#, titles=res_df.columns.values)
    return render_template("result.html",
                            column_names=res_df.columns.values,
                            row_data=list(res_df.values.tolist()),
                            link_column="Patient ID", zip=zip)

if __name__ == '__main__':
    # app.run(host="192.168.10.48", port=5000)
    app.run(host="localhost", port=5000)

# TODO
# 형태소 결정, token 컬럼 저장
# DB 연동
# 최대 출력 갯수, 날짜 지정, 정확도 옵션 추가
# 검색 시간 단축 고민