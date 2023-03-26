package com.test.news.oauth2.provider;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverUserInfoImpl implements OAuthUserInfo {
    private String providerId;
    private String email;
    private String nickname;

    public NaverUserInfoImpl(String providerId, String email, String nickname) {
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getNickname() {
        return nickname;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public NaverUserInfoImpl convertUserInfo(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String providerId = response.get("id").toString();
        String email = response.get("email").toString();
        String nickname = response.get("nickname").toString();

        return new NaverUserInfoImpl(providerId, email, nickname);
    }
}
