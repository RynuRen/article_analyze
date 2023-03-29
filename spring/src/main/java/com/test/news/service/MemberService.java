package com.test.news.service;


import com.test.news.dto.Board;
import com.test.news.dto.PagingResponse;
import com.test.news.oauth2.provider.OAuthProvider;

public interface MemberService {
    // 작성글 불러오기
    PagingResponse<Board> findBoardByMemId(int page, String keyword, String searchType);

    // 닉네임 변경
    void updateMember(Long userId, String nickname);

    // 회원 탈퇴
    void deleteMember(Long userId, OAuthProvider oAuthProvider);
}
