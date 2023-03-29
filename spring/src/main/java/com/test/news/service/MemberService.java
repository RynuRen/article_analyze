package com.test.news.service;

import com.test.news.oauth2.provider.OAuthProvider;

public interface MemberService {
    // 닉네임 변경
    void update(Long userId, String nickname);

    // 회원 탈퇴
    void delete(Long userId, OAuthProvider oAuthProvider);
}
