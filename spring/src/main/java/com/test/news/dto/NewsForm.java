package com.test.news.dto;

// import lombok.Data;

// @Data
// public class NewsForm {

//     private String newsContent;
//     private String newsPress;
//     private String newsStartDate;
//     private String newsEndDate;
//     private String newsLink;
//     private String newsTitle;
//     private String newsDate;
//     private double newsSim;
// }
import java.util.Date;

import lombok.Data;

@Data
public class NewsForm {

    @Data
    public static class request {
        private String newsPress;
        private String newsLink;
        private String newsContent;
        private String newsStartDate;
        private String newsEndDate;

    }

    @Data
    public static class response {
        private String newsPress;
        private String newsLink;
        private String newsTitle;
        private Date newsDate;
        private double newsSim;

    }

}