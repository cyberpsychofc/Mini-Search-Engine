package com.cyberpsych.mini_search_engine.services;

import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final TextProcessorService textProcessorService;
    private final PostingRepository postingRepository;
    private final PageRepository pageRepository;

    public SearchService(TextProcessorService textProcessorService, PostingRepository postingRepository, PageRepository pageRepository){
        this.textProcessorService = textProcessorService;
        this.postingRepository = postingRepository;
        this.pageRepository = pageRepository;
    }
    public List<Map<String, Object>> search(String query){
        logger.info("Processing search query : {}",query);

        List<String> queryTokens = textProcessorService.processText(query);

        Map<Long, Double> pageScores = new HashMap<>();
        for (String term : queryTokens){
            List<PostingEntity> postings = postingRepository.findByTerm(term);

            for (PostingEntity posting : postings){
                // term freq
                double tf = posting.getPositions().size();
                pageScores.merge(posting.getPageId(), tf, Double::sum);
            }
        }

        // formatting response
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : pageScores.entrySet()){
            Long pageId = entry.getKey();
            Double score = entry.getValue();

            pageRepository.findById(pageId).ifPresent(
                    page -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("url", page.getUrl());
                        result.put("score", score);
                        results.add(result);
                    }
            );
        }
        results.sort((a, b) -> Double.compare(
                (Double) b.get("score"),
                (Double) a.get("score")
        ));

        logger.info("Found {} results for query : {}", results.size(), query);
        return results;
    }
    public List<String> autocomplete(String query){
        logger.info("Processing autocomplete query: {}", query);
        if (query == null || query.trim().isEmpty())
            return new ArrayList<>();

        String prefix = query.trim().toLowerCase();
        List<String> suggestions = postingRepository.findTermsByPrefix(prefix);
        logger.info("Found {} suggestions for prefix: {}", suggestions.size(), prefix);
        return suggestions.stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}
