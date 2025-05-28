package com.cyberpsych.mini_search_engine.controllers;

import com.cyberpsych.mini_search_engine.services.WebCrawlerService;
import org.springframework.web.bind.annotation.*;

@RestController
public class CrawlerController {
    private final WebCrawlerService webCrawlerService;

    public CrawlerController(WebCrawlerService webCrawlerService){
        this.webCrawlerService = webCrawlerService;
    }

    @GetMapping("/crawl")
    public String startCrawl(
            @RequestParam
            String seedUrl){
        webCrawlerService.crawl(seedUrl);
        return "Crawling started for: " + seedUrl;
    }
}
