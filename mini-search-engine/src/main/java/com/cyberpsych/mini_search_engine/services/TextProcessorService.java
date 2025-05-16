package com.cyberpsych.mini_search_engine.services;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextProcessorService {
    private final List<String> stopWords;

    public TextProcessorService(@Value("${text-processor.stop-words}") String stopWords) {
        this.stopWords = Arrays.stream(stopWords.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public List<String> processText(String html){
        String cleanText = Jsoup.clean(html, Safelist.none());
        cleanText = cleanText.toLowerCase();
        String tokens[] = cleanText.split("\\W+");

        return Arrays.stream(tokens)
                .filter(token -> !token.isEmpty())
                .filter(token -> !stopWords.contains(token))
                .collect(Collectors.toList());
    }
}
