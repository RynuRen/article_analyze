# 연관글비- 유사도 기반 뉴스 타임라인 커뮤니티
![연관글비로고](https://github.com/RynuRen/article_analyze/blob/main/spring/src/main/resources/static/images/main.png?raw=true)

<br>
<br>

## 프로젝트 소개
[(youtube) 프로젝트 소개 영상](https://youtu.be/koexWXs9tI8)
- 뉴스 소비자들이 사건 및 이슈의 흐름을 이해하고자 할 때 관련된 기사를 일일이 찾아보는 번거로움을 덜기 위해 <br>
자동화된 방법으로 기사를 수집하여 자연어처리를 통해 유사 기사를 추천하고, 사용자 스스로 흐름을 정리할 수 있는 툴을 제공함으로써 <br>
사건의 흐름을 이해하는데 도움을 줄 수 있는 커뮤니티  

<br>
<br>


## 팀원소개

|![gulbi](https://github.com/RynuRen/article_analyze/blob/main/spring/src/main/resources/static/images/gulbi.png?raw=true) |![gulbi_cute](https://github.com/RynuRen/article_analyze/blob/main/spring/src/main/resources/static/images/gulbi_cute.png?raw=true) |![gulbi_error](https://github.com/RynuRen/article_analyze/blob/main/spring/src/main/resources/static/images/gulbi_error.png?raw=true) |![gulbi_smile](https://github.com/RynuRen/article_analyze/blob/main/spring/src/main/resources/static/images/gulbi_smile.png?raw=true) |
|---|---|---|---|
|최은진<br> [**chloeeej**](https://github.com/chloeeej)|곽지섭 <br> [**RynuRen**](https://github.com/RynuRen)|김태성 <br> [**chamgirm**](https://github.com/chamgirm)|박기현 <br>[**park-gihyean**](https://github.com/park-gihyean)|
|product manager<br>model <br>html/css|model, AWS <br>spring boot <br>DB|spring boot <br>html/css|DB <br>model|

<br>
<br>

##  주요기능
![service flow](https://ifh.cc/g/drTOFT.png)

- 키워드 검색 : 우리 서비스에서 키워드를 입력하여 검색하면 daum뉴스 검색결과를 가지고 와 연관기사 검색에 활용
- 유사 기사 검색: 자연어처리를 활용해 사용자로부터 입력받은 기사의 이전 발행일 기사 중 유사한 기사를 추천
- 사용자의 기사 선택: 추천 결과 중 제일 유사하다고 판단한 기사를 선택하여 히스토리를 누적, 선택 기사와 유사한 과거 기사를 계속 따라감으로써 사건의 흐름을 파악 
- 선택 기사 컨텐츠화: 사용자가 선택한 기사들에 직접 코멘트를 달아 사건의 내용을 정리하면 연대표 형식으로 타임라인이 생성되어 게시글화 함 
- 컨텐츠 공유: 사용자들이 작성한 게시글을 게시판에서 공유해 의견을 나누고, 다른 사용자가 작성해둔 게시글을 통해 쉽게 이슈파악을 할 수 있음

<br>
<br>

## 차별점
### 연관 기사 추천
- 같은 날에는 같은 내용의 기사가 많이 나오기 때문에 단순히 유사도가 높은 기사를 추천해주는 것이 아니라 입력받은 기사의 이전 날짜를 비교대상으로 해서 중복기사는 배제하면서 사건의 흐름을 파악하기 용이하게 함

### 모듈화
- 데이터 및 모델을 수정/최신화 하는 과정에서 반복적인 코드 작성이 불가피해 이를 개선하기 위해 데이터 관리 모듈화
- cmd 명령문 입력시 전처리 설정이 들어가있는 json파일을 이용해 소모적인 코드 수정을 줄여 보다 간소화된 데이터의 유지보수 가능
 
### 히스토리 누적
- 서버와 주고받는 내용은 뉴스 id만으로 간소화하고 id를 통해 데이터베이스에 있는 언론사, 날짜, 제목 등 뉴스 메타데이터를 불러옴


<br>
<br>


## 기술스택
- Flask (API 서버) API 서버를 구축하여 Spring Boot로 구현한 WAS에서 받은 요청을 처리하고, 데이터를 반환합니다. 수집된 데이터에서 조건에 맞는 데이터와 입력받은 데이터간의 유사도 연산을 통해 추천 알고리즘을 처리합니다.
- Python (데이터 수집, 추천 알고리즘 처리) 데이터 수집을 위해 Beautiful Soup 라이브러리를 사용하며, 수집한 기사의 본문을 전처리합니다. sklearn 라이브러리의 tf-idf vectorizer를 사용하여 DTM(Documents Term Matrix)을 연산합니다. scrapy 라이브러리로 데이터 수집 pipeline을 구축하여 데이터를 손쉽게 증강하고, 데이터를 최신화합니다. 
- MariaDB (데이터베이스) 수집한 기사들의 메타데이터와 회원 정보, 게시글 정보를 저장합니다.
- Spring Boot (백엔드, 프론트엔드) Thymeleaf 템플릿 엔진을 사용하여 프론트엔드를 구축합니다. Spring Security를 사용하여 Kakao, Naver의 OAuth2.0 인증을 통한 회원가입을 지원합니다. Flask API 서버와 RestTemplate를 이용해 REST API 통신을 합니다. 
- HTML & CSS (프론트엔드 디자인) Bootstrap을 사용하여 프론트엔드 디자인을 개발합니다. HTML을 사용하여 웹 페이지를 구성하고, CSS를 사용하여 스타일링합니다.
- AWS (인프라) 두 개의 EC2에 Flask와 Spring Boot를 각각 백그라운드로 구동합니다. RDS에는 MariaDB로 구축한 데이터를 저장합니다.  



