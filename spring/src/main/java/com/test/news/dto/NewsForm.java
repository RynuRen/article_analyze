package com.test.news.dto;

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
        private Date newsDateRe;

        public Date getNewsDateRe() {
            return this.newsDateRe;
        }

        public void setNewsDateRe(Date newsDateRe) {
            this.newsDateRe = newsDateRe;
        }

        public String getNewsPress() {
            return this.newsPress;
        }

        public void setNewsPress(String newsPress) {
            this.newsPress = newsPress;
        }

        public String getNewsLink() {
            return this.newsLink;
        }

        public void setNewsLink(String newsLink) {
            this.newsLink = newsLink;
        }

        public String getNewsTitle() {
            return this.newsTitle;
        }

        public void setNewsTitle(String newsTitle) {
            this.newsTitle = newsTitle;
        }

        public Date getNewsDate() {
            return this.newsDate;
        }

        public void setNewsDate(Date newsDate) {
            this.newsDate = newsDate;
        }

        public double getNewsSim() {
            return this.newsSim;
        }

        public void setNewsSim(double newsSim) {
            this.newsSim = newsSim;
        }

    }

}