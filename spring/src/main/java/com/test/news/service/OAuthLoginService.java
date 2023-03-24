package com.test.news.service;

import org.springframework.stereotype.Service;

import com.test.news.dto.User;
import com.test.news.mapper.UserMapper;
import com.test.news.model.AuthTokens;
import com.test.news.model.oauth.OAuthInfoResponse;
import com.test.news.model.oauth.OAuthLoginParams;
import com.test.news.model.oauth.RequestOAuthInfoService;
import com.test.news.service.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RequestOAuthInfoService requestOAuthInfoService;

    public AuthTokens login(OAuthLoginParams params) {
        OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
        User user = findOrCreateUser(oAuthInfoResponse);
        return jwtTokenProvider.generateToken(user.getUserId(), user.getRole());
    }

    private User findOrCreateUser(OAuthInfoResponse oAuthInfoResponse) {
        return userMapper.findByEmail(oAuthInfoResponse.getEmail())
            .orElseGet(() -> newUser(oAuthInfoResponse));
    }

    private User newUser(OAuthInfoResponse oAuthInfoResponse) {
        User user = User.builder()
                .email(oAuthInfoResponse.getEmail())
                .nickname(oAuthInfoResponse.getNickname())
                .oAuthProvider(oAuthInfoResponse.getOAuthProvider())
                .build();
        userMapper.joinUser(user);
        return userMapper.findByEmail(user.getEmail()).orElseThrow();
    }
}
