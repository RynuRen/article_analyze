package com.test.news.oauth2.provider;

import java.util.Map;

public interface OAuthUserInfo {

    String getProviderId();

    OAuthProvider getProvider();

    String getEmail();

    String getNickname();

    OAuthUserInfo convertUserInfo(Map<String, Object> attributes);
}