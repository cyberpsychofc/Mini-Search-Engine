package com.cyberpsych.mini_search_engine.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Service
public class WebCrawlerService {
    @Value("${crawler.user-agent}")
    private String userAgent;

    public Document fetchPage(String url) throws IOException{
        return Jsoup.connect(url).get();
    }
}