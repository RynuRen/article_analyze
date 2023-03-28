package com.test.news.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.test.news.mapper.MemberMapper;
import com.test.news.oauth2.provider.OAuthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    @Value("${kakao.admin-key}")
    private String KAKAO_ADMIN_KEY;
    private final String KAKAO_UNLINK_URI = "https://kapi.kakao.com/v1/user/unlink";

    @Autowired
    MemberMapper memberMapper;

    @Override
    public void update(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
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
