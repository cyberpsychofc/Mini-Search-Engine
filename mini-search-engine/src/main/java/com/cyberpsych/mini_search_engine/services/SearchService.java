package com.cyberpsych.mini_search_engine.services;

import com.cyberpsych.mini_search_engine.entities.PageRankEntity;
import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.entities.TermFrequencyEntity;
import com.cyberpsych.mini_search_engine.repositories.PageRankRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import com.cyberpsych.mini_search_engine.repositories.TermFrequencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final TextProcessorService textProcessorService;
    private final PostingRepository postingRepository;
    private final PageRepository pageRepository;
    private final TermFrequencyRepository termFrequencyRepository;
    private final PageRankRepository pageRankRepository;
    private final SynonymService synonymService;
    private final RedisTemplate<String, List<Map<String, Object>>> searchRedisTemplate;
    private final RedisTemplate<String, List<String>> autocompleteRedisTemplate;

    public SearchService(TextProcessorService textProcessorService,
                         PostingRepository postingRepository,
                         PageRepository pageRepository,
                         TermFrequencyRepository termFrequencyRepository,
                         PageRankRepository pageRankRepository,
                         SynonymService synonymService,
                         @Qualifier("searchRedisTemplate") RedisTemplate<String, List<Map<String, Object>>> searchRedisTemplate,
                         @Qualifier("autocompleteRedisTemplate") RedisTemplate<String, List<String>> autocompleteRedisTemplate){
        this.textProcessorService = textProcessorService;
        this.postingRepository = postingRepository;
        this.pageRepository = pageRepository;
        this.termFrequencyRepository = termFrequencyRepository;
        this.pageRankRepository = pageRankRepository;
        this.synonymService = synonymService;
        this.searchRedisTemplate = searchRedisTemplate;
        this.autocompleteRedisTemplate = autocompleteRedisTemplate;
    }

    public List<Map<String, Object>> search(String query){
        logger.info("Processing search query : {}", query);
        String cacheKey = "search:" + query.trim().toLowerCase();
        List<Map<String, Object>> cachedResults = searchRedisTemplate.opsForValue().get(cacheKey);

        if (cachedResults != null && !(cachedResults.isEmpty())){
            logger.info("Cache hit for query: {}", query);
            return cachedResults;
        }

        logger.info("Cache miss for query: {}. Performing search...", query);

        List<String> queryTokens = textProcessorService.processText(query);
        long totalDocuments = pageRepository.count();
        Map<Long, Double> pageScores = new HashMap<>();

        List<String> expandedTokens = new ArrayList<>();
        for (String token : queryTokens) {
            List<String> synonyms = synonymService.getSynonyms(token).stream()
                    .map(syn -> textProcessorService.processText(syn).get(0))
                    .collect(Collectors.toList());
            expandedTokens.addAll(synonyms);
        }
        logger.info("Expanded query tokens : {}", expandedTokens);

        for (String term : expandedTokens){
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
        // pagerank w/ TF-IDF
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : pageScores.entrySet()){
            Long pageId = entry.getKey();
            Double tfIdfscore = entry.getValue();
            PageRankEntity prEntity = pageRankRepository.findById(pageId).orElse(null);
            double pageRankScore = prEntity != null ? prEntity.getPageRankScore() : 1.0 / totalDocuments;
            double finalScore = tfIdfscore * pageRankScore;

            pageRepository.findById(pageId).ifPresent(
                    page -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("url", page.getUrl());
                        result.put("score", finalScore);
                        results.add(result);
                    }
            );
        }
        results.sort((a, b) -> Double.compare(
                (Double) b.get("score"),
                (Double) a.get("score")
        ));

        logger.info("Found {} results for query : {}", results.size(), query);
        searchRedisTemplate.opsForValue().set(cacheKey, results, 10, TimeUnit.MINUTES);
        return results;
    }
    public List<String> autocomplete(String query){
        logger.info("Processing autocomplete query: {}", query);
        String cacheKey = "autocomplete:" + query.trim().toLowerCase();
        List<String> cachedSuggestions = autocompleteRedisTemplate.opsForValue().get(cacheKey);
        if (cachedSuggestions != null && !(cachedSuggestions.isEmpty())){
            logger.info("Cache hit for autocomplete query: {}", query);
            return cachedSuggestions;
        }

        logger.info("Cache miss for autocomplete query: {}. Fetching from database...", query);
        if (query == null || query.trim().isEmpty())
            return new ArrayList<>();

        String prefix = query.trim().toLowerCase();
        List<String> suggestions = postingRepository.findTermsByPrefix(prefix);
        logger.info("Found {} suggestions for prefix: {}", suggestions.size(), prefix);
        List<String> limitedSuggestions = suggestions.stream().limit(10).collect(Collectors.toList());

        autocompleteRedisTemplate.opsForValue().set(cacheKey, limitedSuggestions, 5, TimeUnit.MINUTES);
        return limitedSuggestions;
    }
}
