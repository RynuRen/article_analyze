from flask import Flask, request
from urllib.parse import unquote
from recommand import get_recommend
from preprocessing import content_scraper

def build_result_df(newsPress, newsLink, newsStartDate, newsEndDate):
    startdate = ''.join(list(newsStartDate.split('-')))
    enddate = ''.join(list(newsEndDate.split('-')))
    startdate, enddate = sorted([startdate, enddate]) 
    scrap = content_scraper(newsLink)
    if not scrap:
        return "False"
    cur_title, cur_date, content = scrap
    res_df = get_recommend(content, 10, (startdate, enddate), newsPress)
    if type(res_df) == bool:
        return "False"
    res_df = res_df[['press', 'title', 'date', 'link']]
    res_df['date'] = res_df['date'].dt.strftime('%Y-%m-%d')
    res_df.columns = ['newsPress', 'newsTitle', 'newsDate', 'newsLink']
    res = res_df.to_json(orient="records", force_ascii=False)
    cur_title = cur_title.replace('"', '\\"')
    cur_news = f'{{"newsPress":"{newsPress}", "newsTitle":"{cur_title}", "newsDate":"{cur_date}", "newsLink":"{newsLink}"}}'
    return f'{{"curNews":{cur_news}, "recNews":{res}}}'

app = Flask(__name__)

@app.route("/result", methods=["GET"])
def app_result():
    newsPress = unquote(request.args.get('newsPress'))
    newsLink = request.args.get('newsLink')
    newsStartDate = request.args.get('newsStartDate')
    newsEndDate = request.args.get('newsEndDate')
    res = build_result_df(newsPress, newsLink, newsStartDate, newsEndDate)
    return res

if __name__ == '__main__':
    app.run(host="192.168.10.48", port=5000)
    # app.run(host="localhost", port=5000)