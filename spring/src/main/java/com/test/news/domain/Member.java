package com.test.news.domain;

import java.time.LocalDate;

import com.test.news.oauth2.provider.OAuthProvider;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Member {

    private Long userId;
    private String providerId;
    private String email;
    private String nickname;
    private OAuthProvider oAuthProvider;
    private LocalDate createDate;
    private Role role;

    @Builder
    public Member(Long userId, String providerId, String email, String nickname, OAuthProvider oAuthProvider, LocalDate createDate, Role role) {
        this.userId = userId;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
        this.createDate = createDate;
        this.role = role;
    }
}
