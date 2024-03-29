package com.test.news.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.test.news.dto.Board;
import com.test.news.dto.PagingResponse;
import com.test.news.oauth2.CustomUserDetails;
import com.test.news.oauth2.provider.OAuthProvider;
import com.test.news.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Tag(name = "[@Tag] 유저 컨트롤러")
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "로그인 페이지", description = "[@Operation] 로그인 처리 화면")
    @GetMapping("/login")
    public String login(
            @Parameter(name = "리퀘스트", description = "이전 페이지를 확인하기 위해 리퀘스트를 분석") HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        request.getSession().setAttribute("prevPage", referer);
        return "user/login";
    }

    @Operation(summary = "로그인 성공 페이지", description = "[@Operation] 로그인 성공 처리")
    @GetMapping("/loginSuccess")
    public String loginSucess(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String redirectUrl = "";
        String message = "";
        if (session != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            String nickname = user.getNickname();
            redirectUrl = (String) session.getAttribute("prevPage");
            message = "어서오세요 " + nickname + " 님";
            if (redirectUrl != null) {
                session.removeAttribute("prevPage");
            } else {
                redirectUrl = "/";
            }
        } else {
            redirectUrl = "/";
        }
        model.addAttribute("title", "알림");
        model.addAttribute("text", message);
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", redirectUrl);

        return "message";
}

    @Operation(summary = "접근 제한", description = "[@Operation] 접근 제한 화면")
    @GetMapping("/denied")
    public String denied() {
        return "user/denied";
    }

    @Operation(summary = "유저 정보 페이지", description = "[@Operation] 유저 정보 처리 화면")
    @GetMapping("/info")
    public String info(
            @Parameter(name = "페이지 번호", description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "검색어", description = "검색할 단어", example = "기사") @RequestParam(required = false) String keyword,
            @Parameter(name = "검색타입", description = "검색할 위치", example = "제목") @RequestParam(required = false) String searchType,
            Model model) {
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
            Model model) {
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
            Model model) {
        memberService.deleteMember(userId, oAuthProvider);

        model.addAttribute("title", "알림");
        model.addAttribute("text", "탈퇴가 완료되었습니다.");
        model.addAttribute("buttonText", "확인");
        model.addAttribute("redirectUrl", "/user/logout");

        return "message";
    }
}
