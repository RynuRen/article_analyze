package com.test.news.dto;

import java.time.LocalDate;

import com.test.news.model.Role;
import com.test.news.model.oauth.OAuthProvider;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class User {

    private Long userId;
    private String email;
    private String nickname;
    private OAuthProvider oAuthProvider;
    private LocalDate createDate;
    private Role role;

    @Builder
    public User(Long userId, String email, String nickname, OAuthProvider oAuthProvider, LocalDate createDate, Role role) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.oAuthProvider = oAuthProvider;
        this.createDate = createDate;
        this.role = role;
    }

}
