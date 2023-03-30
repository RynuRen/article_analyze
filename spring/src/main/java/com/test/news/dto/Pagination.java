package com.test.news.dto;

import lombok.Getter;

@Getter
public class Pagination {
    private int totalCount;   // 전체 데이터 수
    private int pageCount;     // 전체 페이지 수
    private int startPage;          // 첫 페이지 번호
    private int endPage;            // 끝 페이지 번호

    public Pagination(int totalCount, int pageNum, int pageSize) {
        this.totalCount = totalCount;
        if (totalCount == 0) {
            this.pageCount = 1;
            this.startPage = 1;
            this.endPage = 1;
        } else {
            this.calculation(pageNum, pageSize);
        }
        
    }

    private void calculation(int pageNum, int pageSize) {
        // 전체 페이지 수 계산
        pageCount = totalCount / pageSize + (totalCount % pageSize == 0 ? 0 : 1);

        // 현재 페이지 번호가 전체 페이지 수보다 큰 경우, 현제 페이지 번호에 전체 페이지 수 저장
        if (pageNum > pageCount) {
            pageNum = pageCount;
        }

        // 첫 페이지 번호 계산
        startPage = ((pageNum - 1) / 10) * 10 + 1; // 시작 페이지 번호 계산

        // 끝 페이지 번호 계산
        endPage = startPage + 9 > pageCount ? pageCount : startPage + 9;

        // 끝 페이지가 전체 페이지 수보다 큰 경우, 끝 페이지 전체 페이지 수 저장
        if (endPage > pageCount) {
            endPage = pageCount;
        }
    }
}
