package com.test.news.service;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.news.dto.NewsForm;
import com.test.news.dto.PagingResponse;

public interface NewsService {
    // 최신 뉴스 가져오기
    PagingResponse<NewsForm.response> newsHj(int page, String query);

    // Flask API 통신
    NewsForm.serviceReturn newsApi(NewsForm.request newsRequest, String selectNewsPress) throws JsonProcessingException;

    // 마지막기간 기사 처리(2020-01-01)
    Map<String, Object> addHistory(NewsForm.request newsRequest) throws JsonProcessingException;
}
