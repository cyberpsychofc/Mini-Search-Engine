package com.cyberpsych.mini_search_engine.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SynonymService {
    private static Logger logger = LoggerFactory.getLogger(SynonymService.class);
    private final Map<String, List<String>> synonymMap = new HashMap<>();

    @PostConstruct
    public void loadSynonym() throws IOException {
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource("synonyms.txt").getInputStream()))){
            String line;
            while ((line = br.readLine()) != null) {
                List<String> synonyms = Arrays.stream(line.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                for (String term : synonyms)
                    synonymMap.put(term, synonyms);
            }
            logger.info("Loaded {} synonym groups", synonymMap.size());
        }
        catch (IOException e) {
            logger.error("Error loading synonyms: {}", e.getMessage());
        }
    }

    public List<String> getSynonyms(String term) {
        return synonymMap.getOrDefault(term, List.of(term));
    }
}
