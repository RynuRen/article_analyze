package com.test.news.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.news.domain.Member;
import com.test.news.mapper.MemberMapper;
import com.test.news.oauth2.provider.OAuthProvider;
import com.test.news.oauth2.provider.OAuthUserInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService extends DefaultOAuth2UserService {
    private final MemberMapper memberMapper;
    private final RequestOAuthInfoService requestOAuthInfoService;

    /**
     * oauth provider에 따라 각각의 userinfo 객체를 불러와 계정 찾기/생성
     * 
     * @param OAuth2UserRequest
     * @return CustomUserDetails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuthProvider provider = OAuthProvider
                .valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuthUserInfo oAuthUserInfo = requestOAuthInfoService.identifyProvider(oAuth2User.getAttributes(), provider);

        Member member = findOrCreateUser(oAuthUserInfo);

        return CustomUserDetails.create(member);
    }

    private Member findOrCreateUser(OAuthUserInfo oAuth2UserInfo) {
        return memberMapper.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> newUser(oAuth2UserInfo));
    }

    @Transactional
    private Member newUser(OAuthUserInfo oAuthUserInfo) {
        Member user = Member.builder()
                .providerId(oAuthUserInfo.getProviderId())
                .email(oAuthUserInfo.getEmail())
                .nickname(oAuthUserInfo.getNickname())
                .oAuthProvider(oAuthUserInfo.getProvider())
                .build();
        memberMapper.joinUser(user);
        return memberMapper.findByEmail(user.getEmail()).orElseThrow();
    }
}
