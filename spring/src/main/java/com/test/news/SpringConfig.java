package com.test.news;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.news.service.NewsService;

@Configuration
public class SpringConfig {
    @Bean
    public NewsService newsService() {
        return new NewsService();

    }
}
