package com.test.news.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class PagingResponse<T> {
    private List<T> list = new ArrayList<>();
    private Pagination pagination;

    public PagingResponse(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }
}
