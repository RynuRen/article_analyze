package com.test.news.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.news.dto.NewsForm;
import com.test.news.dto.PagingResponse;
import com.test.news.mapper.NewsMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    @Autowired
    ObjectMapper objectMapper;
    private final NewsMapper newsMapper;

    @Value("${api-url}")
    private String apiUrl;

    @Override
    public PagingResponse<NewsForm.response> newsHj(int page, String query) {
        String url = String.format("http://%s:5000/daum", apiUrl);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("query", query)
                .queryParam("page", page);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<PagingResponse<NewsForm.response>> responseEntity = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity,
                new ParameterizedTypeReference<PagingResponse<NewsForm.response>>() {
                });

        return responseEntity.getBody();
    }

    @Override
    public NewsForm.serviceReturn newsApi(NewsForm.request newsRequest,
            String selectNewsPress)
            throws JsonProcessingException {
        // API 요청
        NewsForm.apiResponse newsResponse = requestFlask(newsRequest, selectNewsPress);

        int status = newsResponse.getStatus();
        List<NewsForm.response> newsList = null;
        String history = null;
        Map<String, Object> newsHistoryList = null;

        if (status == 0) {
            // API로 돌려받은 값 처리
            newsList = newsSort(newsResponse);

            // History 처리
            NewsForm.historyForm newsHistoryForm = newsHistory(newsRequest, newsResponse.getCurNews());

            // History DB 조회
            List<Integer> idList = newsHistoryForm.getIdList();
            List<NewsForm.response> newsHisList = new ArrayList<>();
            if (!idList.isEmpty()) {
                newsHisList = newsMapper.findHisAll(idList);
            }
            // History에 첫 검색뉴스 + 이후 검색뉴스 저장
            newsHistoryList = new HashMap<>();
            newsHistoryList.put("curNews", newsHistoryForm.getCurNews());
            newsHistoryList.put("newsHisList", newsHisList);

            history = objectMapper.writeValueAsString(newsHistoryForm);
        } else {
            history = String.valueOf(status);
        }

        // return values -> dto
        NewsForm.serviceReturn serviceReturn = new NewsForm.serviceReturn();
        serviceReturn.setNewsList(newsList);
        serviceReturn.setToNext(history);
        serviceReturn.setHisNews(newsHistoryList);

        return serviceReturn;
    }

    // Flaks API
    private NewsForm.apiResponse requestFlask(NewsForm.request newsRequest,
            String selectNewsPress) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        if (newsRequest.getNewsId() == 0) {
            String url = String.format("http://%s:5000/search", apiUrl);
            // String url = "http://192.168.10.48:5000/search";
            builder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("newsPress", newsRequest.getNewsPress())
                    .queryParam("newsLink", newsRequest.getNewsLink())
                    .queryParam("newsStartDate", newsRequest.getNewsStartDate())
                    .queryParam("newsEndDate", newsRequest.getNewsEndDate());
        } else {
            String url = String.format("http://%s:5000/research", apiUrl);
            // String url = "http://192.168.10.48:5000/research";
            builder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("newsPress", newsRequest.getNewsPress())
                    .queryParam("newsId", newsRequest.getNewsId())
                    .queryParam("newsStartDate", newsRequest.getNewsStartDate())
                    .queryParam("newsEndDate", newsRequest.getNewsEndDate())
                    .queryParam("selectNewsPress", selectNewsPress);

        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<NewsForm.apiResponse> responseEntity = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, NewsForm.apiResponse.class);

        return responseEntity.getBody();
    }

    // 추천 뉴스 정렬
    private List<NewsForm.response> newsSort(NewsForm.apiResponse newsResponse) {
        Map<Integer, Double> idSim = newsResponse.getRecNews();
        List<Integer> newsIdList = new ArrayList<>(idSim.keySet());
        List<NewsForm.response> newsList = newsMapper.findAll(newsIdList);
        // 리스트에 유사도 넣기
        for (int i = 0; i < newsList.size(); i++) {
            newsList.get(i).setNewsSim(idSim.get(newsList.get(i).getNewsId()));
        }
        // 유사도 순 정렬
        Collections.sort(newsList);

        return newsList;
    }

    // 검색 결과 저장
    private NewsForm.historyForm newsHistory(NewsForm.request newsRequest, NewsForm.response curNews)
            throws JsonProcessingException {
        String newsHistoryStr = newsRequest.getNewsHistory();
        NewsForm.historyForm newsHistoryForm = new NewsForm.historyForm();
        // 검색 결과에 뉴스 id 갱신
        if (newsHistoryStr == null) {
            newsHistoryForm.setCurNews(curNews);
            List<Integer> idList = new ArrayList<Integer>();
            newsHistoryForm.setIdList(idList);
        } else {
            newsHistoryForm = objectMapper.readValue(newsHistoryStr, NewsForm.historyForm.class);
            List<Integer> idList = newsHistoryForm.getIdList();
            idList.add(newsRequest.getNewsId());
        }

        return newsHistoryForm;
    }

    public NewsForm.serviceReturn addHistory(NewsForm.request newsRequest)
            throws JsonProcessingException {
        NewsForm.serviceReturn serviceReturn = new NewsForm.serviceReturn();
        String newsHistoryStr = newsRequest.getNewsHistory();

        NewsForm.historyForm newsHistoryForm = objectMapper.readValue(newsHistoryStr, NewsForm.historyForm.class);
        List<Integer> idList = newsHistoryForm.getIdList();
        idList.add(newsRequest.getNewsId());

        List<NewsForm.response> newsHisList = newsMapper.findHisAll(idList);
        Map<String, Object> newsHistoryList = null;
        newsHistoryList = new HashMap<>();
        newsHistoryList.put("curNews", newsHistoryForm.getCurNews());
        newsHistoryList.put("newsHisList", newsHisList);
        String history = objectMapper.writeValueAsString(newsHistoryForm);

        serviceReturn.setToNext(history);
        serviceReturn.setHisNews(newsHistoryList);

        return serviceReturn;
    }

}