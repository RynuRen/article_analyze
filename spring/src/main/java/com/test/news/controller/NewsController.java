package com.test.news.controller;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.news.dto.NewsForm;
import com.test.news.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/")
    public String main() {
        return "main";
    }

    @GetMapping("/query_search")
    public String fsearch(NewsForm.query query, Model model) {
        List<NewsForm.response> newsList = newsService.newsHj(query);

        model.addAttribute("newsList", newsList);
        return "query_search";
    }

    @GetMapping("/search")
    public String search(NewsForm.request newsRequest, Model model,
            @RequestParam("selectNewsPress") String selectNewsPress, HttpServletRequest request,
            HttpServletResponse response) throws JsonProcessingException {
        String referer = request.getHeader("Referer");
        NewsForm.serviceReturn newsRes = newsService.newsApi(newsRequest, selectNewsPress);

        if (newsRes.getHisNews() == null) {
            int status = Integer.parseInt(newsRes.getToNext());
            String errorMessage = "";

            if (status == 1) {
                errorMessage = "해당 언론사는 지원하지 않습니다.";
            } else if (status == 2) {
                errorMessage = "해당 기사는 너무 짧습니다.";
            } else if (status == 3) {
                errorMessage = "해당 기사를 연결할 수 없습니다.";
            } else {
                errorMessage = "UNKNOWN 에러다 에러!";
            }
            
            model.addAttribute("title", "알림");
            model.addAttribute("text", errorMessage);
            model.addAttribute("buttonText", "확인");
            model.addAttribute("redirectUrl", referer);

            return "message";
        }
        model.addAttribute("newsList", newsRes.getNewsList());
        model.addAttribute("newsHistory", newsRes.getToNext());
        model.addAttribute("history", newsRes.getHisNews());

        return "search";
    }

}