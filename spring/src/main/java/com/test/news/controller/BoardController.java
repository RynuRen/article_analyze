package com.test.news.controller;

import java.io.IOException;
import java.util.List;

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

@Controller
@RequestMapping("board")
public class BoardController {

    @Autowired
    BoardMapper boardMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    NewsMapper newsMapper;

    @GetMapping("write")
    public String boardWriteForm() {

        return "board/boardwrite";
    }

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

    @PostMapping("writepro")
    public String boardWritePro(Board board, Model model) {
        boardMapper.write(board);
        model.addAttribute("title", "알림");
        model.addAttribute("text", "작성이 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/board/list");
        return "message";
    }

    @GetMapping("list")
    public String getList(Model model, @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String searchType) {
        int pageSize = 10; // 페이지당 글 개수
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
        model.addAttribute("list", list);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("keyword", keyword); // 검색어를 뷰로 전달
        return "board/boardList";
    }

    @GetMapping("view")
    public String boardView(Model model, Integer id) {

        List<Integer> newsList = boardMapper.selectByNewsId(id);
        model.addAttribute("history", newsMapper.findHisAll(newsList));
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

    @PostMapping("update/{id}")
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
