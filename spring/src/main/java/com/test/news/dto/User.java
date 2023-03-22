package com.test.news.dto;

import java.time.LocalDate;

import com.test.news.authentication.domain.oauth.OAuthProvider;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {

    private Long userId;
    private String email;
    private String nickname;
    private OAuthProvider oAuthProvider;
    private LocalDate createDate;

    @Builder
    public User(String email, String nickname, OAuthProvider oAuthProvider) {
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
    }

}
