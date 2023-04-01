package com.test.news.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.news.dto.Board;
import com.test.news.dto.NewsForm;
import com.test.news.dto.NewsForm.response;
import com.test.news.mapper.BoardMapper;
import com.test.news.mapper.NewsMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("board")
public class BoardController {

    @Autowired
    BoardMapper boardMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    NewsMapper newsMapper;

    @GetMapping("hiswrite")
    public String boardHisWrite(NewsForm.serviceReturn newsRes, Model model) throws IOException {

        String newsSelect = newsRes.getToNext();
        NewsForm.historyForm newsHis = objectMapper.readValue(newsSelect, NewsForm.historyForm.class);
        List<response> selHisnews = newsMapper.findHisAll(newsHis.getIdList());
        NewsForm.response beCurNews = newsHis.getCurNews();
        Board curNews = new Board();
        curNews.setBoardNewsPress(beCurNews.getNewsPress());
        curNews.setBoardNewsLink(beCurNews.getNewsLink());
        curNews.setBoardNewsTitle(beCurNews.getNewsTitle());
        curNews.setBoardNewsDate(beCurNews.getNewsDate());
        String curNewsStr = objectMapper.writeValueAsString(curNews);
        String selNewsStr = objectMapper.writeValueAsString(newsHis.getIdList());

        model.addAttribute("curNews", curNews);
        model.addAttribute("history", selHisnews);
        model.addAttribute("curNewsStr", curNewsStr);
        model.addAttribute("selNewsStr", selNewsStr);

        return "board/boardhiswrite";

    }

    @PostMapping("hiswritepro")
    public String boardHisWirte(Board board, Model model, @RequestParam("boardComment") List<String> boardNewsComment)
            throws IOException {

        Board boardwrite = objectMapper.readValue(board.getCurNews(), Board.class);
        boardwrite.setBoardContent(board.getBoardContent());
        boardwrite.setBoardTitle(board.getBoardTitle());
        boardwrite.setBoardWriterId(board.getBoardWriterId());
        List<Integer> boardIdList = objectMapper.readValue(board.getSelNews(), new TypeReference<List<Integer>>() {
        });
        boardIdList.add(0, 0);

        boardMapper.write(boardwrite);
        boardMapper.putList(boardwrite.getBoardId(), boardIdList, boardNewsComment);

        model.addAttribute("title", "알림");
        model.addAttribute("text", "작성이 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/board/list");

        return "message";
    }

    @GetMapping("list")
    public String getList(Model model, @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String searchType)
            throws IOException {
        int pageSize = 9; // 페이지당 글 개수
        int startRow = (pageNum - 1) * pageSize; // 시작 행 번호
        List<Board> list = null;
        int totalCount = 0;
        int pageCount = 0;
        int startPage = 0;
        int endPage = 0;
        if (keyword != null && !keyword.isEmpty()) { // 키워드 검색
            list = boardMapper.searchBoardList(keyword, searchType, startRow, pageSize); // DB에서 키워드 검색
            totalCount = boardMapper.getBoardCountByKeyword(keyword); // 검색된 글 개수
        } else { // 일반 목록
            list = boardMapper.getBoardList(startRow, pageSize); // DB에서 목록 조회
            totalCount = boardMapper.getBoardCount(); // 전체 글 개수 조회
        }

        pageCount = totalCount / pageSize + (totalCount % pageSize == 0 ? 0 : 1); // 전체 페이지 개수 계산
        startPage = ((pageNum - 1) / 10) * 10 + 1; // 시작 페이지 번호 계산
        endPage = startPage + 9 > pageCount ? pageCount : startPage + 9; // 끝 페이지 번호 계산

        List<String> newsSrcList = new ArrayList<>();
        String defaultImageUrl = "https://post-phinf.pstatic.net/MjAxODAyMjZfMTM4/MDAxNTE5NjI0MTMyMTI0.FWwP0Zwb9nBLXkloL8awQT6WLGYTbCAUtkzD-oISGvAg.U8oEPBRh6i1vgVvyM2jE2iNYJ-XWXSnMQKm84DhFpqIg.JPEG/20180226%EB%B3%B4%EB%8F%84%EA%B8%B0%EC%82%ACa984_%281%29.jpg";

        for (Board board : list) {
            String newsLink = board.getBoardNewsLink();
            Document doc = Jsoup.connect(newsLink).get();
            String imageUrl = "";
            try {

                Element articleBody = doc.selectFirst("div.article_view > section");
                Element image = articleBody.select("img").first();
                imageUrl = (image != null) ? image.attr("src") : defaultImageUrl;

            } catch (Exception e) {
                imageUrl = defaultImageUrl;
            }

            newsSrcList.add(imageUrl);
        }

        model.addAttribute("newsSrcList", newsSrcList);
        model.addAttribute("list", list);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("keyword", keyword); // 검색어를 뷰로 전달
        return "board/boardList";
    }

    @GetMapping("/list/view")
    public String boardView(Model model, Integer id) throws IOException {

        List<Integer> newsList = boardMapper.selectByNewsId(id);
        List<response> newsIn = newsMapper.findHisAll(newsList);
        List<String> newsSrcList = new ArrayList<>();
        Board boardInfo = boardMapper.selectById(id);
        NewsForm.response curNewsIn = new response();
        curNewsIn.setNewsDate(boardInfo.getBoardNewsDate());
        curNewsIn.setNewsLink(boardInfo.getBoardNewsLink());
        curNewsIn.setNewsPress(boardInfo.getBoardNewsPress());
        curNewsIn.setNewsTitle(boardInfo.getBoardNewsTitle());
        newsIn.add(0, curNewsIn);

        String defaultImageUrl = "https://post-phinf.pstatic.net/MjAxODAyMjZfMTM4/MDAxNTE5NjI0MTMyMTI0.FWwP0Zwb9nBLXkloL8awQT6WLGYTbCAUtkzD-oISGvAg.U8oEPBRh6i1vgVvyM2jE2iNYJ-XWXSnMQKm84DhFpqIg.JPEG/20180226%EB%B3%B4%EB%8F%84%EA%B8%B0%EC%82%ACa984_%281%29.jpg";

        for (NewsForm.response response : newsIn) {
            String newsLink = response.getNewsLink();
            Document doc = Jsoup.connect(newsLink).get();
            String imageUrl = "";
            try {

                Element articleBody = doc.selectFirst("div.article_view > section");
                Element image = articleBody.select("img").first();
                imageUrl = (image != null) ? image.attr("src") : defaultImageUrl;

            } catch (Exception e) {
                imageUrl = defaultImageUrl;
            }

            newsSrcList.add(imageUrl);
        }

        model.addAttribute("newsSrcList", newsSrcList);
        model.addAttribute("history", newsIn);
        model.addAttribute("curNews", boardMapper.selectById(id));
        model.addAttribute("comment", boardMapper.selectByComment(id));
        return "board/boardview";

    }

    @PostMapping("delete")
    public String boardDelete(Integer id, Model model) {

        boardMapper.delete(id);
        model.addAttribute("title", "알림");
        model.addAttribute("text", "삭제가 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/board/list");

        return "message";
    }

    @GetMapping("modify/{id}")
    public String modify(@PathVariable("id") Integer id, Model model) {

        List<Integer> newsList = boardMapper.selectByNewsId(id);
        model.addAttribute("history", newsMapper.findHisAll(newsList));
        model.addAttribute("curNews", boardMapper.selectById(id));
        model.addAttribute("comment", boardMapper.selectByComment(id));
        return "board/boardmodify";
    }

    @PostMapping("modify/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, @RequestParam("boardComment") List<String> boardComment,
            Board board, Model model) {

        Board boardTemp = boardMapper.selectById(id);
        boardTemp.setBoardTitle(board.getBoardTitle());
        boardTemp.setBoardContent(board.getBoardContent());

        List<Integer> newsIdList = boardMapper.selectByNewsId(id);
        boardMapper.update(boardTemp);
        boardMapper.updateComment(boardComment, id, newsIdList);

        model.addAttribute("title", "알림");
        model.addAttribute("text", "수정이 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/board/list");

        return "message";
    }

}
