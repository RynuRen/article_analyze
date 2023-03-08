package com.test.news.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
        // System.out.println(newsList);
        model.addAttribute("newsList", newsList.getRecNews());

        String newsHistoryStr = newsRequest.getNewsHistory();
        if (newsHistoryStr == null) {
            newsHistoryStr = "[]";
        }

        // System.out.println("newsHistory");
        System.out.println(newsHistoryStr);
        List<NewsForm.response> newsHistory = objectMapper.readValue(newsHistoryStr,
                new TypeReference<List<NewsForm.response>>() {
                });

        newsHistory.add(newsList.getCurNews());

        String history = objectMapper.writeValueAsString(newsHistory);

        model.addAttribute("newsHistory", history);
        model.addAttribute("history", newsHistory);

        return "/news";
    }

}