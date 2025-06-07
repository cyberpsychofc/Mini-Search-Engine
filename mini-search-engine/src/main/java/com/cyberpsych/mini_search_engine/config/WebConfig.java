package com.cyberpsych.mini_search_engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfig() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/**")
                        .allowedOrigins("https://mini-search-engine.vercel.app", "116.203.129.16", "116.203.134.67", "23.88.105.37", "128.140.8.200", "91.99.23.109")
                        .allowedMethods(HttpMethod.OPTIONS.name(), HttpMethod.GET.name(), HttpMethod.POST.name())
                        .allowedHeaders(HttpHeaders.CONTENT_TYPE,
                                HttpHeaders.AUTHORIZATION)
                        .allowCredentials(true);
            }
        };
    }
}
