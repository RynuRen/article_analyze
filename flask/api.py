from flask import Flask, request
from urllib.parse import unquote
from recommand import get_fst_recommend, get_recommend
from preprocessing import cur_news_scraper
from daum_search import search_rst

def build_fst_result_df(news_press, news_link, news_startdate, news_enddate):
    startdate = ''.join(list(news_startdate.split('-')))
    enddate = ''.join(list(news_enddate.split('-')))
    startdate, enddate = sorted([startdate, enddate])
    scrap = cur_news_scraper(news_link)
    if not scrap:
        return "False"
    cur_news, content = scrap

    id_list = get_fst_recommend(content, 10, (startdate, enddate), news_press)
    if type(id_list) == bool:
        return "False"
    res = {}
    res['curNews'] = cur_news
    print(id_list)
    res['recNews'] = id_list
    return res

def build_result_df(news_press, news_id, news_startdate, news_enddate):
    startdate = ''.join(list(news_startdate.split('-')))
    enddate = ''.join(list(news_enddate.split('-')))
    startdate, enddate = sorted([startdate, enddate])

    id_list = get_recommend(news_id, 10, (startdate, enddate), news_press)
    if type(id_list) == bool:
        return "False"
    res = {}
    res['recNews'] = id_list
    return res

app = Flask(__name__)

@app.route("/search", methods=["GET"])
def app_search():
    news_press = unquote(request.args.get('newsPress'))
    news_link = request.args.get('newsLink')
    news_startdate = request.args.get('newsStartDate')
    news_enddate = request.args.get('newsEndDate')
    res = build_fst_result_df(news_press, news_link, news_startdate, news_enddate)
    return res

@app.route("/research", methods=["GET"])
def app_research():
    news_press = unquote(request.args.get('newsPress'))
    news_id = request.args.get('newsId')
    news_startdate = request.args.get('newsStartDate')
    news_enddate = request.args.get('newsEndDate')
    res = build_result_df(news_press, news_id, news_startdate, news_enddate)
    return res

@app.route("/daum", methods=["GET"])
def app_daum():
    query = unquote(request.args.get('query'))
    page = request.args.get('page')
    res = search_rst(query, page)
    return res

if __name__ == '__main__':
    app.run(host="192.168.10.48", port=5000, debug=True)
    # app.run(host="localhost", port=5000, debug=True)
    # app.run(host="0.0.0.0", port=5000)