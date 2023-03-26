package com.test.news.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[@Tag] 유저 컨트롤러")
@Controller
@RequestMapping("/user")
public class UserController {
    @Value("${was-url}")
    private String wasUrl;

    @Operation(summary = "로그인 페이지", description = "[@Operation] 로그인 처리 화면")
    @GetMapping("/login")
    public String login(
            @Parameter(name = "모델", description = "view에 넘기기 위한 model", example = "localhost") Model model) {
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
    public String info() {
        return "user/info";
    }
}
