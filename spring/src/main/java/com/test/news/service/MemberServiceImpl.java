package com.test.news.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.test.news.domain.Member;
import com.test.news.mapper.MemberMapper;
import com.test.news.oauth2.CustomUserDetails;
import com.test.news.oauth2.provider.OAuthProvider;

import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    @Value("${kakao.admin-key}")
    private String KAKAO_ADMIN_KEY;
    private final String KAKAO_UNLINK_URI = "https://kapi.kakao.com/v1/user/unlink";
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public void update(Long userId, String nickname) {
        Member member = memberMapper.findById(userId).orElseThrow();
        Member memberModified = Member.builder()
                                    .userId(userId)
                                    .providerId(member.getProviderId())
                                    .email(member.getEmail())
                                    .nickname(nickname)
                                    .oAuthProvider(member.getOAuthProvider())
                                    .createDate(member.getCreateDate())
                                    .role(member.getRole())
                                    .build();
        memberMapper.update(memberModified);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(CustomUserDetails.create(memberModified), authentication.getCredentials(), authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @Override
    @Transactional
    public void delete(Long userId, OAuthProvider oAuthProvider) {
        String providerId = memberMapper.findPIDById(userId);

        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();

        // oAuthProvider에 따라 분류 추가해야뎀
        headers.add("Authorization", "KakaoAK " + KAKAO_ADMIN_KEY);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.set("target_id_type", "user_id");
        param.set("target_id", providerId);

        HttpEntity<MultiValueMap<String, Object>> restRequest = new HttpEntity<>(param, headers);
        restTemplate.postForEntity(KAKAO_UNLINK_URI, restRequest, JSONObject.class);

        memberMapper.deleteById(userId);
    }
}