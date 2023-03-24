// package com.test.news.authentication.domain;

// import java.util.Date;

// import org.springframework.stereotype.Component;

// import com.test.news.model.AuthTokens;
// import com.test.news.service.security.JwtTokenProvider;

// import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class AuthTokensGenerator {
//     private static final String BEARER_TYPE = "Bearer";
//     private static final long ACCESS_TOKEN_EXPIRE_TIME = 100 * 60 * 30; // 30min
//     private static final long REFRESH_TOKEN_EXPIRE_TIME = 100 * 60 * 30; // 30min // 1000 * 60 * 60 * 24 * 7; // 7day

//     private final JwtTokenProvider jwtTokenProvider;

//     // 사용자 식별값으로 access token 생성
//     public AuthTokens generate(Long userId) {
//         long now = (new Date()).getTime();
//         Date accessTokenExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
//         Date refreshToeknExpiredAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

//         String subject = userId.toString();
//         String accessToken = jwtTokenProvider.generate(subject, accessTokenExpiredAt);
//         String refreshToken = jwtTokenProvider.generate(subject, refreshToeknExpiredAt);

//         return AuthTokens.of(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000L);
//     }

//     // access token에서 사용자 식별값 추출
//     public Long extractUserId(String accessToken) {
//         return Long.valueOf(jwtTokenProvider.extractSubject(accessToken));
//     }
// }
