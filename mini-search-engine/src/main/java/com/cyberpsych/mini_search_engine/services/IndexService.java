package com.cyberpsych.mini_search_engine.services;

import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.models.Posting;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndexService {
    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    @Value("${crawler.user-agent}")
    private String userAgent;
    private final Map<String, List<Posting>> invertedIndex = new HashMap<>();
    private final PageRepository pageRepository;
    private final TextProcessorService textProcessorService;

    public IndexService(PageRepository pageRepository, TextProcessorService textProcessorService){
        this.pageRepository = pageRepository;
        this.textProcessorService = textProcessorService;
    }
    public void addPosting(String term, Posting posting){
        invertedIndex.computeIfAbsent(term, k-> new ArrayList<>()).add(posting);
        logger.debug("Add posting for term '{}': pageId = {}, positions = {}",
                term,
                posting.getPageId(),
                posting.getPositions()
        );
    }
    public List<Posting> getPostings(String term){
        return invertedIndex.getOrDefault(term, new ArrayList<>());
    }
    public void clearIndex(){
        invertedIndex.clear();
        logger.info("Inverted index cleared.");
    }

    @Scheduled(fixedDelay = 300000)
    public void buildIndex(){
        logger.info("Starting Inverted Index Build");
        clearIndex();

        List<Page> pages = pageRepository.findAll();
        for (Page page: pages){
            try {
                Document doc = Jsoup.connect(page.getUrl())
                        .userAgent(userAgent)
                        .timeout(5000)
                        .get();

                List<String> tokens = textProcessorService.processText(doc.html());

                for (int i=0; i < tokens.size(); i++){
                    String term = tokens.get(i);
                    List<Integer> positions = new ArrayList<>();
                    positions.add(i + 1);
                    Posting posting = new Posting(page.getId(), positions);
                    addPosting(term, posting);
                }
            }
            catch (IOException e){
                logger.error("Error indexing page {}: {}", page.getUrl(), e.getMessage());
            }
        }
        logger.info("Inverted index built with {} terms", invertedIndex.size());
    }
}
