package com.test.news.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/fsearch")
    public String fsearch(NewsForm.query query, Model model) {
        List<NewsForm.response> newsList = newsService.newsHj(query);

        model.addAttribute("newsList", newsList);
        return "fsearch";
    }

    @GetMapping("/search")
    public String search(NewsForm.request newsRequest, Model model, HttpServletRequest request,
            HttpServletResponse response) throws JsonProcessingException {

        NewsForm.serviceReturn newsRes = newsService.newsApi(newsRequest);

        model.addAttribute("newsList", newsRes.getNewsList());
        model.addAttribute("newsHistory", newsRes.getToNext());
        model.addAttribute("history", newsRes.getHisNews());

        return "search";
    }

}