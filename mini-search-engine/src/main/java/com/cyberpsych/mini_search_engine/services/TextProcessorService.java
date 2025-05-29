package com.cyberpsych.mini_search_engine.services;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextProcessorService {
    private final List<String> stopWords;
    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);

    public TextProcessorService(@Value("${text-processor.stop-words}") String stopWords) {
        this.stopWords = Arrays.stream(stopWords.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private String stemToken(String token){
        englishStemmer es = new englishStemmer();
        es.setCurrent(token);
        es.stem();
        return es.getCurrent();
    }

    public List<String> processText(String html){
        String cleanText = Jsoup.clean(html, Safelist.none());
        cleanText = cleanText.toLowerCase();
        String tokens[] = cleanText.split("\\W+");
        List<String> stemmedTokens = Arrays.stream(tokens)
                .filter(token -> !token.isEmpty())
                .filter(token -> !stopWords.contains(token))
                .map(this::stemToken)
                .limit(100)
                .collect(Collectors.toList());
        logger.debug("Processed tokens: {}", stemmedTokens);
        return stemmedTokens;
    }
}
