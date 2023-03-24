package com.test.news.model.oauth;

public interface OAuthApiClient {

    // client 타입 반환
    OAuthProvider oAuthProvider();

    // auth code으로 인증 api를 요청, access token 획득
    String requestAccessToken(OAuthLoginParams params);

    // access token으로 email, nickname이 포함된 profile 정보 획등
    OAuthInfoResponse requestOauthInfo(String accessToken);
}
