package com.cyberpsych.mini_search_engine.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerConfig {

    @Value("${crawler.rate-limit}")
    private double rateLimit;

    @Bean
    public RateLimiter rateLimiter(){
        return RateLimiter.create(rateLimit);
    }
}
