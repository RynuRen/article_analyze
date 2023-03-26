package com.test.news.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.test.news.domain.Member;

import lombok.Getter;
import lombok.ToString;

// OAuthUserService의 반환타입
@Getter
@ToString
public class CustomUserDetails implements UserDetails, OAuth2User {
    private Long userId;
    private String nickname;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public CustomUserDetails(Long userId, String nickname, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    public static CustomUserDetails create(Member member) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole().toString()));

        CustomUserDetails userDetails = new CustomUserDetails(
                                            member.getUserId(),
                                            member.getNickname(),
                                            authorities);

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("email", member.getEmail());
        attributes.put("oAuthProvider", member.getOAuthProvider());
        attributes.put("createDate", member.getCreateDate());

        userDetails.setAttributes(attributes);

        return userDetails;
    }

    /**
     * UserDetails: 권한 목록
     * 
     * @return authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * UserDetails: 비밀번호
     * 
     * @return null
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * UserDetails: 닉네임
     * 
     * @return email
     */
    @Override
    public String getUsername() {
        return nickname;
    }

    /**
     * UserDetails: 계정만료
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * UserDetails: 계정잠김
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * UserDetails: 비밀번호 만료
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * UserDetails: 계정 활성화
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * OAuth2User 구현
     * 
     * @return attributes
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /*
     * OAuth2User 구현
     * 
     * @return (String) userId
     */
    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
