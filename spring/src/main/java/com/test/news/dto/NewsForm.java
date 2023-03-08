package com.test.news.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class NewsForm {

    @Data
    public static class request {
        private String newsPress;
        private String newsLink;
        private LocalDate newsStartDate;
        private LocalDate newsEndDate;
        private String newsHistory;

    }

    @Data
    public static class response {
        private String newsPress;
        private String newsLink;
        private String newsTitle;
        private LocalDate newsDate;

    }

    @Data
    public static class cookieRequest {
        private String newsPress;
        private String newsLink;
        private LocalDate newsEndDate;
        private String newsTitle;

    }

    @Data
    public static class apiResponse {
        private response curNews;
        private List<response> recNews;
    }

}