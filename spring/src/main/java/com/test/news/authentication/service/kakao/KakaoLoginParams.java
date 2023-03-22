package com.test.news.authentication.service.kakao;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.test.news.authentication.domain.oauth.OAuthLoginParams;
import com.test.news.authentication.domain.oauth.OAuthProvider;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoLoginParams implements OAuthLoginParams {
    private String authorizationCode;

    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        return body;
    }
}
