package com.test.news.service;

import java.util.List;

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
import com.test.news.dto.Board;
import com.test.news.dto.Pagination;
import com.test.news.dto.PagingResponse;
import com.test.news.mapper.MemberMapper;
import com.test.news.oauth2.CustomUserDetails;
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
    private final MemberMapper memberMapper;

    /**
     * 작성글 불러오기
     * 
     * @param pageNum    현제 페이지
     * @param keyword    검색 키워드
     * @param searchType 검색 타입
     * @return list & pagination 정보
     */
    @Override
    public PagingResponse<Board> findBoardByMemId(int pageNum, String keyword, String searchType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long userId = Long.parseLong(user.getName());
        int pageSize = 10; // 페이지당 글 개수
        int startRow = (pageNum - 1) * pageSize; // 시작 행 번호
        int totalCount = 0;
        List<Board> list = null;
        Pagination pagination = null;

        if (keyword != null && !keyword.isEmpty()) { // 키워드 검색
            totalCount = memberMapper.countByMemIdByKeyword(userId, keyword); // 검색된 글 개수
            pagination = new Pagination(totalCount, pageNum, pageSize);
            list = memberMapper.findBoardByMemIdByKeyword(userId, keyword, searchType, startRow, pageSize); // DB에서 키워드
                                                                                                            // 검색
        } else { // 일반 목록
            totalCount = memberMapper.countByMemId(userId); // 전체 글 개수 조회
            pagination = new Pagination(totalCount, pageNum, pageSize);
            list = memberMapper.findBoardByMemId(userId, startRow, pageSize); // DB에서 목록 조회
        }
        return new PagingResponse<>(list, pagination);
    }

    /**
     * 닉네임 변경
     * 
     * @param userId   유저ID
     * @param nickname 바꿀 닉네임
     */
    @Override
    @Transactional
    public void updateMember(Long userId, String nickname) {
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
        Authentication newAuth = new UsernamePasswordAuthenticationToken(CustomUserDetails.create(memberModified),
                authentication.getCredentials(), authentication.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    /**
     * 회원 탈퇴
     * 
     * @param userId        유저ID
     * @param oAuthProvider 인증 제공사
     */
    @Override
    @Transactional
    public void deleteMember(Long userId, OAuthProvider oAuthProvider) {
        String providerId = memberMapper.findPIDById(userId);

        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();

        // oAuthProvider에 따라 accessToken을 provider에서 재발급 받아 그 token으로 처리하는걸로 수정해야뎀-git코드
        // 되돌리자..
        if (oAuthProvider == OAuthProvider.KAKAO) {
            headers.add("Authorization", "KakaoAK " + KAKAO_ADMIN_KEY);
            headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.set("target_id_type", "user_id");
            param.set("target_id", providerId);

            HttpEntity<MultiValueMap<String, Object>> restRequest = new HttpEntity<>(param, headers);
            restTemplate.postForEntity(KAKAO_UNLINK_URI, restRequest, JSONObject.class);
        } else if (oAuthProvider == OAuthProvider.NAVER) {
            log.info("네이버 회원 탈퇴 구현중..");
        }

        memberMapper.deleteById(userId);
    }
}
