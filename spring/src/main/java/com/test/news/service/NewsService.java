package com.test.news.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.news.dto.NewsForm;

@Service
public class NewsService {

    @Autowired
    ObjectMapper objectMapper;
    
    public NewsForm.apiResponse newsApi(NewsForm.request newsRequest) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:5000/result";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("newsPress", newsRequest.getNewsPress())
                .queryParam("newsLink", newsRequest.getNewsLink())
                .queryParam("newsStartDate", newsRequest.getNewsStartDate())
                .queryParam("newsEndDate", newsRequest.getNewsEndDate());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, String.class);
        NewsForm.apiResponse newsResponse = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>() {
        });

        return newsResponse;
    }

    public NewsForm.history newsStack(NewsForm.request newsRequest, NewsForm.response curNews) throws JsonProcessingException {
        String newsHistoryStr = newsRequest.getNewsHistory();
        if (newsHistoryStr == null) {
            newsHistoryStr = "[]";
        }

        List<NewsForm.response> newsHistoryList = objectMapper.readValue(newsHistoryStr, new TypeReference<List<NewsForm.response>>() {});

        newsHistoryList.add(curNews);

        String history = objectMapper.writeValueAsString(newsHistoryList);
        NewsForm.history newsHistory = new NewsForm.history();
        newsHistory.setHisNews(newsHistoryList);
        newsHistory.setToNext(history);

        return newsHistory;
    }

}
