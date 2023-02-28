package com.test.news.service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.news.dto.NewsForm;

public class NewsService {
    //// POST 방식
    // public List<NewsForm.response> newsApi(NewsForm.request newsRequest) throws
    // JsonProcessingException {
    // HttpHeaders httpHeaders = new HttpHeaders();
    // httpHeaders.setContentType(new MediaType("application", "json",
    // Charset.forName("UTF-8")));

    // ObjectMapper objectMapper = new ObjectMapper();

    // String newsString = objectMapper.writeValueAsString(newsRequest);
    // System.out.println(newsString);

    // HttpEntity<String> entity = new HttpEntity<>(newsString, httpHeaders);

    // RestTemplate restTemplate = new RestTemplate();
    // ResponseEntity<String> responseEntity =
    // restTemplate.exchange("http://192.168.10.175:5000/result",
    // HttpMethod.GET, entity, String.class);

    // System.out.println(responseEntity.getStatusCode());
    // System.out.println(responseEntity.getBody());

    // List<NewsForm.response> newsResponse =
    // objectMapper.readValue(responseEntity.getBody(), new TypeReference<>() {
    // });
    // System.out.println(newsResponse);
    // return newsResponse;
    // }
    public List<NewsForm.response> newsApi(NewsForm.request newsRequest) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://172.30.1.50:5000/result";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("newsPress", newsRequest.getNewsPress())
                .queryParam("newsLink", newsRequest.getNewsLink())
                .queryParam("newsContent", newsRequest.getNewsContent())
                .queryParam("newsStartDate", newsRequest.getNewsStartDate())
                .queryParam("newsEndDate", newsRequest.getNewsEndDate());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        List<NewsForm.response> newsResponse = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>() {
        });

        return newsResponse;
    }

    public List<NewsForm.response> getModifiedNewsList(List<NewsForm.response> newsList) {
        List<NewsForm.response> modifiedNewsList = new ArrayList<>();

        for (NewsForm.response news : newsList) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(news.getNewsDate());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Date modifiedNewsDate = calendar.getTime();

            NewsForm.response modifiedNews = new NewsForm.response();
            modifiedNews.setNewsPress(news.getNewsPress());
            modifiedNews.setNewsLink(news.getNewsLink());
            modifiedNews.setNewsTitle(news.getNewsTitle());
            modifiedNews.setNewsSim(news.getNewsSim());
            modifiedNews.setNewsDate(news.getNewsDate());
            modifiedNews.setNewsDateRe(modifiedNewsDate);

            modifiedNewsList.add(modifiedNews);
        }

        return modifiedNewsList;
    }

}
