package com.test.news.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.test.news.dto.NewsForm.response;

@Mapper
public interface NewsMapper {
    public List<response> findAll(List<Integer> newsIdList);

    public List<response> findHisAll(List<Integer> newsIdList);
}
