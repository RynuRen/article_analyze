package com.test.news.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.test.news.dto.User;

@Mapper
public interface UserMapper {
    void joinUser(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId);

    List<User> findAll();

}
