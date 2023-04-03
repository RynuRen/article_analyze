package com.test.news.controller;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.news.dto.NewsForm;
import com.test.news.dto.Pagination;
import com.test.news.dto.PagingResponse;
import com.test.news.dto.NewsForm.serviceReturn;
import com.test.news.service.NewsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[@Tag] 뉴스 컨트롤러")
@Controller
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @Operation(summary = "메인 페이지", description = "[@Operation] 메인 화면")
    @GetMapping("/")
    public String main() {
        return "main";
    }

    @Operation(summary = "쿼리 검색", description = "[@Operation] 쿼리 검색 화면")
    @GetMapping("/query_search")
    public String fsearch(
            @Parameter(name = "pageNum", description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "query", description = "검색할 내용", example = "검색") String query,
            Model model, HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        PagingResponse<NewsForm.response> queryResponse = newsService.newsHj(pageNum, query);

        int status = queryResponse.getStatus();
        if (status == 4) {
            String errorMessage = "검색 결과가 없습니다.";

            model.addAttribute("title", "알림");
            model.addAttribute("text", errorMessage);
            model.addAttribute("buttonText", "확인");
            model.addAttribute("redirectUrl", referer);

            return "message";
        }
        List<NewsForm.response> newsList = queryResponse.getList();
        Pagination pagination = queryResponse.getPagination();
        pagination = new Pagination(pagination.getPageCount(), pageNum);

        model.addAttribute("query", query);
        model.addAttribute("newsList", newsList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("pageNum", pageNum);
        return "query_search";
    }

    @Operation(summary = "뉴스 검색", description = "[@Operation] 뉴스 검색 화면")
    @GetMapping("/search")
    public String search(
            @Parameter(name = "newsRequest", description = "검색 뉴스 정보") NewsForm.request newsRequest,
            @Parameter(name = "selectNewsPress", description = "검색할 언론사", example = "") @RequestParam("selectNewsPress") String selectNewsPress,
            HttpServletRequest request, HttpServletResponse response, Model model) throws JsonProcessingException {
        String referer = request.getHeader("Referer");

        NewsForm.serviceReturn newsRes = new serviceReturn();

        if (newsRequest.getNewsEndDate().isBefore(LocalDate.parse("2020-01-01"))) {
            NewsForm.serviceReturn newsFinal = newsService.addHistory(newsRequest);
            newsRes.setHisNews(newsFinal.getHisNews());
            newsRes.setToNext(newsFinal.getToNext());

        } else {
            newsRes = newsService.newsApi(newsRequest, selectNewsPress);

        }

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