package com.cyberpsych.mini_search_engine.services;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Service
public class WebCrawlerService {
    @Value("${crawler.user-agent}")
    private String userAgent;
    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);

    public Document fetchPage(String url) throws IOException{
        logger.info("Fetching URL: {}",url);
        Document doc = Jsoup.connect(url).userAgent(userAgent).get();
        logger.debug("Fetched document from {} with title: {}",url, doc.title());
        return Jsoup.connect(url).get();
    }
    public Set<String> extractLinks(String url) throws IOException{
        logger.info("Extracting links from {}",url);
        Set<String> links = new HashSet<>();
        Document doc = fetchPage(url);

        for(Element link: doc.select("a[href]")){
            String href = link.attr("abs:href");
            if (!href.isEmpty())
                links.add(href);
        }
        return links;
    }
}