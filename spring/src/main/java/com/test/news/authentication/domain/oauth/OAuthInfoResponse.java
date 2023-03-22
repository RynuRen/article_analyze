package com.test.news.authentication.domain.oauth;

public interface OAuthInfoResponse {
    String getEmail();

    String getNickname();

    OAuthProvider getOAuthProvider();
}
