package com.test.news.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.test.news.dto.Board;
import com.test.news.dto.PagingResponse;
import com.test.news.oauth2.provider.OAuthProvider;
import com.test.news.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Tag(name = "[@Tag] 유저 컨트롤러")
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {
    @Value("${was-url}")
    private String wasUrl;
    private final MemberService memberService;

    @Operation(summary = "로그인 페이지", description = "[@Operation] 로그인 처리 화면")
    @GetMapping("/login")
    public String login(
            HttpServletRequest request,
            @Parameter(name = "모델", description = "view에 넘기기 위한 model") Model model) {
        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("prevPage", referrer);
        model.addAttribute("wasUrl", wasUrl);
        return "user/login";
    }

    @Operation(summary = "접근 제한", description = "[@Operation] 접근 제한 화면")
    @GetMapping("/denied")
    public String denied() {
        return "user/denied";
    }

    @Operation(summary = "유저 정보 페이지", description = "[@Operation] 유저 정보 처리 화면")
    @GetMapping("/info")
    public String info(
            @Parameter(name = "페이지 번호", description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "검색어", description = "검색할 단어", example = "기사")
            @RequestParam(required = false) String keyword,
            @Parameter(name = "검색타입", description = "검색할 위치", example = "제목")
            @RequestParam(required = false) String searchType,
            @Parameter(name = "모델", description = "view에 넘기기 위한 model") Model model) {
        PagingResponse<Board> response = memberService.findBoardByMemId(pageNum, keyword, searchType);

        model.addAttribute("response", response);
        model.addAttribute("pageNum", pageNum);
        
        return "user/info";
    }

    @Operation(summary = "유저 닉네임 변경", description = "[@Operation] 유저 닉네임 변경 처리")
    @PostMapping("/update")
    public String update(
            @Parameter(name = "유저ID", description = "닉네임 변경할 유저ID", example = "1") Long userId,
            @Parameter(name = "닉네임", description = "변경할 닉네임", example = "닉네임") String nickname,
            @Parameter(name = "모델", description = "view에 넘기기 위한 model") Model model) {
        memberService.updateMember(userId, nickname);

        model.addAttribute("title", "알림");
        model.addAttribute("text", "변경이 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/user/info");

        return "message";
    }

    @Operation(summary = "유저 탈퇴", description = "[@Operation] 유저 탈퇴 처리")
    @PostMapping("/delete")
    public String delete(
            @Parameter(name = "유저ID", description = "삭제할 유저ID", example = "1") Long userId,
            @Parameter(name = "인증 제공자", description = "인증을 제공한 플렛폼", example = "KAKAO") OAuthProvider oAuthProvider,
            @Parameter(name = "모델", description = "view에 넘기기 위한 model") Model model) {
        memberService.deleteMember(userId, oAuthProvider);

        model.addAttribute("title", "알림");
        model.addAttribute("text", "탈퇴가 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/user/logout");

        return "message";
    }
}
