package com.test.news.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

import com.test.news.domain.Member;

@Mapper
public interface MemberMapper {

    @Transactional
    void joinUser(Member user);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long userId);

    List<Member> findAll();
}
