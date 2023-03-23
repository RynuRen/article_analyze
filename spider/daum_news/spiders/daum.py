from daum_news.items import DaumNewsItem
import time
import re

press_list = ['경향신문', '국민일보', '뉴스1', '뉴시스', '동아일보', '문화일보', '서울신문', '세계일보', '연합뉴스',  '조선일보', '중앙일보', '한겨레', '한국일보']

stop_content = ["무단전재", "재배포금지","저작권자 ⓒ 서울신문사","무단복제 및 전재","무단 전재 및 재배포","제보는 카톡", "☞", "무단 전재-재배포", "▶연합뉴스 앱 지금 바로 다운받기~"]

rep_list = ['기사내용 요약']

stop_title = ['[부고]', '[인사]', '[표]', '[모멘트]', '[궂긴소식]']

press_dictionary = {'경향신문':1, '국민일보':2, '뉴스1':3, '뉴시스':4, '동아일보':5, '문화일보':6, '서울신문':7, '세계일보':8, '연합뉴스':9,  '조선일보':10, '중앙일보':11, '한겨레':12, '한국일보':13}


import scrapy
class DaumSpider(scrapy.Spider):
    custom_settings = {}
    name = 'daum'
    allowed_domains = ['news.daum.net','v.daum.net']

    
    def __init__(self, category=None, date=None, *args, **kwargs):
        super(DaumSpider, self).__init__(*args, **kwargs)
        self.category = category
        self.date = date
        self.start_urls = [f'https://news.daum.net/breakingnews/{self.category}?page=999&regDate={self.date}']
        
        
        with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"start {self.category} {self.date}, ")
    def start_requests(self):
        self.start_time = time.time()
        for url in self.start_urls:
            yield scrapy.Request(url=url, callback=self.parse)

    def parse(self, response):
        
        
        lastnum = response.xpath('//em[@class="num_page"]').extract()
        if not lastnum:
            with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"finished page is not exist \n")
            self.close_spider(reason='finished')
            return
        lastnum =re.sub(r'[^0-9]', '', lastnum[0])
        lastnum = int(lastnum)
        
        
        
        for page in range(1, lastnum+1):
            url = f'https://news.daum.net/breakingnews/{self.category}?page={page}&regDate={self.date}'
            yield response.follow(url, callback=self.parse_page)
            
            
            
    def parse_page(self, response):
        news_allocated = response.xpath('//strong[@class="tit_thumb"]')
        
        for new_allocated in news_allocated:
            press = new_allocated.xpath('./span[@class="info_news"]/text()').extract_first()
            if press == None:
                continue
            
            press_id = press_dictionary.get(press,0)
            if press_id == 0:
                continue
                
            title = new_allocated.xpath('./a/text()').extract_first()
            
            tflag = True
            for t in stop_title:
                if t in title:
                    tflag = False
                    break
            if not tflag:
                continue
                
            link = new_allocated.xpath('./a/@href').extract_first()
            
            
            yield response.follow(link,
                                  callback=self.parse_news,
                                  meta={'press_id': press_id,
                                        'title': title,
                                        'link':link})
            
    
    def parse_news(self, response):
        generlist = response.xpath('//span[@class="txt_info"]/text()').extract_first()
        
        if generlist == "입력 " or generlist == "기자":
            return
            
        particle_content = response.xpath('//div[@dmcf-ptype="general"]/text()|//p[@dmcf-ptype="general"]/text()').extract()
        
        
        rst = []

        for particle in particle_content:
            for tmp in particle.split('\n'):
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
            return
        
        press_id = response.meta['press_id']
        title = response.meta['title']
        link = response.meta['link']
        
        item = DaumNewsItem()
        item["press_id"] = press_id
        item["title"] = title
        item["content"] = content
        item["link"] = link
        
        yield item
        
        
    def my_errback(self, failure):
        request = failure.request
        retry_times = request.meta.get('retry_times', 0)
        if retry_times < self.RETRY_TIMES:
            retry_times += 1
            self.logger.warning(f'Retrying request {request.url} (retry_times={retry_times})')
            yield request.copy(meta={'retry_times': retry_times})
        else:
            url = failure.request.url
            self.logger.error(f'Retry failed for {request.url} after {self.RETRY_TIMES} retries')
            
            
            with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"Retry failed for {request.url} after {self.RETRY_TIMES} retries \n")
                
    def close(self):
        end_time = time.time()
        elapsed_time = end_time - self.start_time
        with open("./log/Log_scraping.txt", "a") as f:
                f.write(f"running time : {elapsed_time}\n")
            

                
        
            


