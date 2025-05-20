package com.cyberpsych.mini_search_engine.controller;

import com.cyberpsych.mini_search_engine.controllers.SearchController;
import com.cyberpsych.mini_search_engine.services.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
public class SearchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @Test
    void shouldReturnSearchResults() throws Exception {
        List<Map<String, Object>> results = List.of(
                Map.of("url", "https://example.org", "score", 2.0),
                Map.of("url", "https://example.com", "score", 1.0)
        );
        when(searchService.search("quick fox")).thenReturn(results);

        mockMvc.perform(get("/api/search?q=quick fox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("https://example.org"))
                .andExpect(jsonPath("$[0].score").value(2.0))
                .andExpect(jsonPath("$[1].url").value("https://example.com"))
                .andExpect(jsonPath("$[1].score").value(1.0));
    }
}
