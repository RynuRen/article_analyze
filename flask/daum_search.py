from preprocessing import create_soup

def search_rst(query, page=1):
    from datetime import datetime
    url = f"https://search.daum.net/search?w=news&cluster=n&q={query}&p={page}"
    
    newlist_soup = create_soup(url)
    newslist_html = newlist_soup.find("ul", attrs={"class":"list_news"})
    news_list = newslist_html.find_all("a", attrs={"class":"tit_main fn_tit_u"})
    news_info = newslist_html.find_all("span", attrs={"class":"cont_info"})
    
    newslist = []
    for i in range(10):
        news = {}
        news['newsTitle'] = news_list[i].text
        
        newslink = news_list[i]["href"]
        if newslink.startswith('https://v.daum.net/v/'):
            newslink = newslink.rstrip("?f=o")
        news['newsLink'] = newslink
        
        date = news_info[i].find("span").text
        try:
            date = datetime.strptime(date, '%Y.%m.%d').strftime('%Y-%m-%d')
        except:
            date = datetime.now().strftime('%Y-%m-%d')
        news['newsDate'] = date
        news['newsPress'] = news_info[i].find_all("a")[0].text
        # news_ok = news_info[4].find_all("a")[1].text # 다음뉴스 체크
        newslist.append(news)
        
    return newslist