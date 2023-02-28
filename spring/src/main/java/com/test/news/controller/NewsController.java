package com.test.news.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.news.dto.NewsForm;
import com.test.news.service.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewsController {

    @Autowired
    NewsService newsService;

    @GetMapping("/")
    public String search() {
        return "/search";
    }

    @GetMapping("/search")
    public String result(NewsForm.request newsRequest, Model model) throws JsonProcessingException {
        List<NewsForm.response> newsList = newsService.newsApi(newsRequest);
        System.out.println(newsList);
        model.addAttribute("newsList", newsList);
        List<NewsForm.response> modifiedNewsList = newsService.getModifiedNewsList(newsList);
        model.addAttribute("modifiednewsList", modifiedNewsList);

        return "/news";

    }

}