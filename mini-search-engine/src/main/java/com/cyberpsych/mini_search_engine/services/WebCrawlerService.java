package com.cyberpsych.mini_search_engine.services;
import com.cyberpsych.mini_search_engine.entities.Link;
import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.repositories.LinkRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.google.common.util.concurrent.RateLimiter;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URL;
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
    private final RateLimiter rateLimiter;

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);

    public WebCrawlerService(PageRepository pageRepository, LinkRepository linkRepository, RateLimiter rateLimiter){
        this.pageRepository = pageRepository;
        this.linkRepository = linkRepository;
        this.rateLimiter = rateLimiter;
    }

    public boolean isAllowedByRobots(String url) {
        try {
            URL u = new URL(url);
            String baseUrl = u.getProtocol() + "://" + u.getHost();
            String path = u.getPath();

            Connection.Response res = Jsoup.connect(baseUrl + "/robots.txt")
                    .userAgent(userAgent).timeout(10000)
                    .ignoreContentType(true).execute();

            String[] lines = res.body().split("\\r?\\n");

            boolean inOurSection = false;
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty()) continue;

                // removing comments
                int c = line.indexOf('#');
                if (c != -1) line = line.substring(0, c).trim();

                if (line.toLowerCase().startsWith("user-agent:")) {
                    String agent = line.substring(11).trim();
                    // reset and then set only if it's ours or wildcard
                    inOurSection = agent.equals("*") || agent.equalsIgnoreCase(userAgent);
                }
                else if (inOurSection && line.toLowerCase().startsWith("disallow:")) {
                    String dis = line.substring(9).trim();
                    if (!dis.isEmpty() && path.startsWith(dis)) {
                        logger.info("URL disallowed by robots.txt: {}", url);
                        return false;
                    }
                }
            }
            return true;
        }
        catch (IOException e) {
            logger.warn("Could not fetch robots.txt ({}): {} – allowing by default.", url, e.getMessage());
            return true;
        }
    }
    public Document fetchPage(String url) throws IOException{
        logger.info("Fetching URL: {}",url);
        rateLimiter.acquire();
        Document doc = Jsoup.connect(url).userAgent(userAgent).timeout(10000).get();
        logger.debug("Fetched document from {} with title: {}",url, doc.title());
        return doc;
    }
    public Set<String> extractLinks(String url) throws IOException{
        logger.info("Extracting links from {}",url);
        Set<String> links = new HashSet<>();
        Document doc = fetchPage(url);

        for(Element link: doc.select("a[href]")){
            String href = link.attr("abs:href");
            if (!href.isEmpty() && href.startsWith("http") && (href.length() < 255)) // DB character limit
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

        if (!isAllowedByRobots(seedUrl)){
            logger.warn("Seed URL {} is disallowed by robots.txt. Aborting crawl.", seedUrl);
            return;
        }
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