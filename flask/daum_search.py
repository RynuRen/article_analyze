from preprocessing import create_soup

def create_selenium_soup(url):
    from bs4 import BeautifulSoup
    from selenium import webdriver
    from selenium.webdriver.chrome.service import Service
    from selenium.webdriver.chrome.options import Options
    from webdriver_manager.chrome import ChromeDriverManager
    
    options = Options()
    options.add_argument('headless')
    options.add_argument("disable-gpu")
    options.add_argument('--blink-settings=imagesEnabled=false')
    options.add_argument("disable-infobars")
    options.add_argument("--disable-extensions")

    # 속도 향상을 위한 옵션 해제
    prefs = {'profile.default_content_setting_values': {'cookies' : 2, 'images': 2, 'plugins' : 2, 'popups': 2, 'geolocation': 2, 'notifications' : 2, 'auto_select_certificate': 2, 'fullscreen' : 2, 'mouselock' : 2, 'mixed_script': 2, 'media_stream' : 2, 'media_stream_mic' : 2, 'media_stream_camera': 2, 'protocol_handlers' : 2, 'ppapi_broker' : 2, 'automatic_downloads': 2, 'midi_sysex' : 2, 'push_messaging' : 2, 'ssl_cert_decisions': 2, 'metro_switch_to_desktop' : 2, 'protected_media_identifier': 2, 'app_banner': 2, 'site_engagement' : 2, 'durable_storage' : 2}}   
    options.add_experimental_option('prefs', prefs)

    browser = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
    browser.get(url)
    soup = BeautifulSoup(browser.page_source, 'html.parser')
    
    return soup

def search_rst(query, page=1):
    from datetime import datetime
    
    res = {}
    url = f"https://search.daum.net/search?w=news&cluster=n&q={query}&p=250"
    soup = create_selenium_soup(url)
    pageCount = soup.find_all("a", attrs ={"class": "btn_page"})[-1].text
    pagination = {}
    pagination['pageCount'] = pageCount
    res['pagination'] = pagination
    
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
    res['list'] = newslist
    return res