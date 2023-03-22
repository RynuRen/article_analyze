package com.test.news.authentication;

import org.springframework.stereotype.Service;

import com.test.news.authentication.domain.AuthTokens;
import com.test.news.authentication.domain.AuthTokensGenerator;
import com.test.news.authentication.domain.oauth.OAuthInfoResponse;
import com.test.news.authentication.domain.oauth.OAuthLoginParams;
import com.test.news.authentication.domain.oauth.RequestOAuthInfoService;
import com.test.news.dto.User;
import com.test.news.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {
    private final UserMapper userMapper;
    private final AuthTokensGenerator authTokensGenerator;
    private final RequestOAuthInfoService requestOAuthInfoService;

    public AuthTokens login(OAuthLoginParams params) {
        OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
        Long userId = findOrCreateUser(oAuthInfoResponse);
        return authTokensGenerator.generate(userId);
    }

    private Long findOrCreateUser(OAuthInfoResponse oAuthInfoResponse) {
        return userMapper.findByEmail(oAuthInfoResponse.getEmail())
                .map(User::getUserId)
                .orElseGet(() -> newUser(oAuthInfoResponse));
    }

    private Long newUser(OAuthInfoResponse oAuthInfoResponse) {
        User user = User.builder()
                .email(oAuthInfoResponse.getEmail())
                .nickname(oAuthInfoResponse.getNickname())
                .oAuthProvider(oAuthInfoResponse.getOAuthProvider())
                .build();
        userMapper.joinUser(user);
        return userMapper.findByEmail(user.getEmail()).orElseThrow().getUserId();
    }
}
