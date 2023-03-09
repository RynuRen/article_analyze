package com.test.news.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.news.dto.NewsForm;
import com.test.news.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewsController {

    @Autowired
    NewsService newsService;
    @Autowired
    ObjectMapper objectMapper;

    @GetMapping("/")
    public String search() {
        return "search";
    }

    @GetMapping("/search")
    public String result(NewsForm.request newsRequest, Model model, HttpServletRequest request,
            HttpServletResponse response) throws JsonProcessingException {

        // api 처리
        NewsForm.apiResponse newsList = newsService.newsApi(newsRequest);
        model.addAttribute("newsList", newsList.getRecNews());

        // history 처리
        NewsForm.history newsHistory = newsService.newsStack(newsRequest, newsList.getCurNews());
        String newsHistoryStr = newsHistory.getToNext();
        List<NewsForm.response> history = newsHistory.getHisNews();

        model.addAttribute("newsHistory", newsHistoryStr);
        model.addAttribute("history", history);

        return "news";
    }

}