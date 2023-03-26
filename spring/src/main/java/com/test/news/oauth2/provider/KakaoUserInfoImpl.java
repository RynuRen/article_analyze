package com.test.news.oauth2.provider;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoUserInfoImpl implements OAuthUserInfo {
    private String providerId;
    private String email;
    private String nickname;

    public KakaoUserInfoImpl(String providerId, String email, String nickname) {
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
        return OAuthProvider.KAKAO;
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
    public KakaoUserInfoImpl convertUserInfo(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String providerId = attributes.get("id").toString();
        String email = kakaoAccount.get("email").toString();
        String nickname = profile.get("nickname").toString();

        return new KakaoUserInfoImpl(providerId, email, nickname);
    }
}
