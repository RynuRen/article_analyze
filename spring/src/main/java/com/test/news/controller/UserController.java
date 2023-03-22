package com.test.news.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.news.authentication.OAuthLoginService;
import com.test.news.authentication.domain.AuthTokens;
import com.test.news.authentication.domain.AuthTokensGenerator;
import com.test.news.authentication.service.kakao.KakaoLoginParams;
import com.test.news.authentication.service.naver.NaverLoginParams;
import com.test.news.dto.User;
import com.test.news.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserMapper userMapper;
    private final AuthTokensGenerator authTokensGenerator;
    private final OAuthLoginService oAuthLoginService;

    @Value("${api-url}")
    private String apiUrl; 

    // @GetMapping
    // public ResponseEntity<List<User>> findAll() {
    //     return ResponseEntity.ok(userMapper.findAll());
    // }

    // @GetMapping("/{accessToekn}")
    // public ResponseEntity<User> findByAccessToken(@PathVariable String accessToken) {
    //     Long userId = authTokensGenerator.extractUserId(accessToken);
    //     return ResponseEntity.ok(userMapper.findById(userId).get());
    // }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("apiUrl", apiUrl);
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
