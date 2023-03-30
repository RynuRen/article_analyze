package com.test.news.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.news.dto.NewsForm;

public interface NewsService {
    // 최신 뉴스 가져오기
    List<NewsForm.response> newsHj(NewsForm.query query);

    // Flask API 통신
    NewsForm.serviceReturn newsApi(NewsForm.request newsRequest, String selectNewsPress) throws JsonProcessingException;
}
