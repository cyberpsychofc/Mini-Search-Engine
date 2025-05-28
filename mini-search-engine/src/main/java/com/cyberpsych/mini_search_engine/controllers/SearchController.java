package com.cyberpsych.mini_search_engine.controllers;

import com.cyberpsych.mini_search_engine.services.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService){
        this.searchService = searchService;
    }
    @GetMapping("/api/search")
    public List<Map<String, Object>> search(
            @RequestParam
            String q){
        return searchService.search(q);
    }

    @GetMapping("/api/autocomplete")
    public List<String> autocomplete(@RequestParam String q){
        return searchService.autocomplete(q);
    }
}
