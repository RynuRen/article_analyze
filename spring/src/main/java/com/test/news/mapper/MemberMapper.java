package com.test.news.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.test.news.domain.Member;
import com.test.news.dto.Board;

@Mapper
public interface MemberMapper {

    void joinUser(Member user);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long userId);
    
    String findPIDById(Long userId);

    void deleteById(Long userId);

    void update(Member member);

    int countByMemId(Long userId);

    List<Board> findBoardByMemId(@Param("userId")Long userId, @Param("startRow")int startRow, @Param("pageSize")int pageSize);

    int countByMemIdByKeyword(@Param("userId")Long userId, @Param("keyword")String keyword);

    List<Board> findBoardByMemIdByKeyword(@Param("userId")Long userId, @Param("keyword")String keyword, @Param("searchType")String searchType, @Param("startRow")int startRow, @Param("pageSize")int pageSize);
    
}
