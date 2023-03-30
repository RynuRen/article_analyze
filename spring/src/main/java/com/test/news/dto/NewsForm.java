package com.test.news.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class NewsForm {
    @Data
    public static class query {
        private String query;
        private int page;
    }

    @Data
    public static class request {
        private int newsId;
        private String newsPress;
        private String newsLink;
        private LocalDate newsStartDate;
        private LocalDate newsEndDate;
        private String newsHistory;

    }

    @Data
    public static class response implements Comparable<response> {
        private int newsId;
        private String newsPress;
        private String newsLink;
        private String newsTitle;
        private LocalDate newsDate;
        private double newsSim;

        @Override
        public int compareTo(response response) {
            if (response.newsSim > newsSim) {
                return 1;
            } else if (response.newsSim < newsSim) {
                return -1;
            }
            return 0;
        }

    }

    @Data
    public static class apiResponse {
        private int status;
        private response curNews;
        private Map<Integer, Double> recNews;
    }

    @Data
    public static class historyForm {
        private response curNews;
        private List<Integer> idList;
    }

    @Data
    public static class serviceReturn {
        private List<NewsForm.response> newsList;
        private String toNext;
        private Map<String, Object> hisNews;
    }

}