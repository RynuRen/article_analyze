package com.test.news.dto;

import java.time.LocalDate;
import java.util.Date;

import lombok.Data;

@Data
public class Board {
    private Integer boardId;
    private String boardTitle;
    private String boardContent;
    private String boardWriterId;
    private Date boardCreateDate;
    private Date boardUpdateDate;
    private String boardNewsPress;
    private String boardNewsTitle;
    private String boardNewsLink;
    private LocalDate boardNewsDate;
    private String curNews;
    private String selNews;

}
