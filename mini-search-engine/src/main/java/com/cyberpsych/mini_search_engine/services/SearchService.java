package com.cyberpsych.mini_search_engine.services;

import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.entities.TermFrequencyEntity;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import com.cyberpsych.mini_search_engine.repositories.TermFrequencyRepository;
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
    private final TermFrequencyRepository termFrequencyRepository;

    public SearchService(TextProcessorService textProcessorService, PostingRepository postingRepository, PageRepository pageRepository, TermFrequencyRepository termFrequencyRepository){
        this.textProcessorService = textProcessorService;
        this.postingRepository = postingRepository;
        this.pageRepository = pageRepository;
        this.termFrequencyRepository = termFrequencyRepository;
    }
    public List<Map<String, Object>> search(String query){
        logger.info("Processing search query : {}",query);

        List<String> queryTokens = textProcessorService.processText(query);
        long totalDocuments = pageRepository.count();
        Map<Long, Double> pageScores = new HashMap<>();

        for (String term : queryTokens){
            List<PostingEntity> postings = postingRepository.findByTerm(term);
            TermFrequencyEntity tfEntity = termFrequencyRepository.findById(term).orElse(null);

            double idf = (tfEntity != null && tfEntity.getDocumentFrequency() > 0) ? Math.log((double) totalDocuments / tfEntity.getDocumentFrequency()) : 0.0;

            for (PostingEntity posting : postings){
                // term freq
                double tf = posting.getPositions().size();
                double tf_idf = tf * idf;
                pageScores.merge(posting.getPageId(), tf_idf, Double::sum);
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
