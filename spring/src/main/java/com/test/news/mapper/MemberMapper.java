package com.test.news.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.test.news.domain.Member;

@Mapper
public interface MemberMapper {

    void joinUser(Member user);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long userId);
    
    String findPIDById(Long userId);

    List<Member> findAll();

    void deleteById(Long userId);

    void update(Member member);
}
