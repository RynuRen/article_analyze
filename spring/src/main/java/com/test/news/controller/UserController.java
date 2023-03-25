package com.test.news.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.news.model.AuthTokens;
import com.test.news.service.OAuthLoginService;
import com.test.news.service.kakao.KakaoLoginParams;
import com.test.news.service.naver.NaverLoginParams;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final OAuthLoginService oAuthLoginService;

    @Value("${was-url}")
    private String wasUrl; 

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("wasUrl", wasUrl);
        System.out.println(model);
        return "user/login";
    }

    @GetMapping("/kakao/callback")
    @ResponseBody
    public ResponseEntity<AuthTokens> loginKakao(@RequestParam String code) {
        KakaoLoginParams params = new KakaoLoginParams();
        params.setAuthorizationCode(code);
        return ResponseEntity.ok(oAuthLoginService.login(params));
    }

    @GetMapping("/naver/callback")
    @ResponseBody
    public ResponseEntity<AuthTokens> naverCallback(@RequestParam String code, @RequestParam String state) {
        NaverLoginParams params = new NaverLoginParams();
        params.setAuthorizationCode(code);
        params.setState(state);
        return ResponseEntity.ok(oAuthLoginService.login(params));
    }

}
