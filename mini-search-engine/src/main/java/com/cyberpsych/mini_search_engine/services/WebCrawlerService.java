package com.cyberpsych.mini_search_engine.services;
import com.cyberpsych.mini_search_engine.entities.Link;
import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.repositories.LinkRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Service
public class WebCrawlerService {
    @Value("${crawler.user-agent}")
    private String userAgent;
    private final PageRepository pageRepository;
    private final LinkRepository linkRepository;

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);

    public WebCrawlerService(PageRepository pageRepository, LinkRepository linkRepository){
        this.pageRepository = pageRepository;
        this.linkRepository = linkRepository;
    }

    public Document fetchPage(String url) throws IOException{
        logger.info("Fetching URL: {}",url);
        Document doc = Jsoup.connect(url).userAgent(userAgent).get();
        logger.debug("Fetched document from {} with title: {}",url, doc.title());
        return doc;
    }
    public Set<String> extractLinks(String url) throws IOException{
        logger.info("Extracting links from {}",url);
        Set<String> links = new HashSet<>();
        Document doc = fetchPage(url);

        for(Element link: doc.select("a[href]")){
            String href = link.attr("abs:href");
            if (!href.isEmpty() && href.startsWith("http"))
                links.add(href);
        }
        logger.info("Found {} links on {}", links.size(), url);
        return links;
    }
    public void crawl(String seedUrl){
        Queue<Page> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        Page seedPage = new Page();
        seedPage.setUrl(seedUrl);
        pageRepository.save(seedPage);

        queue.add(seedPage);
        visited.add(seedUrl);

        while(!queue.isEmpty()){
            Page currentPage = queue.poll();

            try{
                Set<String> links = extractLinks(currentPage.getUrl());
                for (String linkUrl : links){
                    if (!visited.contains(linkUrl)){
                        
                        //save linked page
                        Page linkedPage = new Page();
                        linkedPage.setUrl(linkUrl);
                        linkedPage = pageRepository.save(linkedPage);

                        //save link relationship
                        Link link = new Link();
                        link.setFrom(currentPage);
                        link.setTo(linkedPage);
                        linkRepository.save(link);

                        // track visited pages
                        queue.add(linkedPage);
                        visited.add(linkUrl);
                    }
                }
            }
            catch (IOException e){
                logger.error("Error crawling {}",currentPage.getUrl(), e.getMessage());
            }
        }
    }
}