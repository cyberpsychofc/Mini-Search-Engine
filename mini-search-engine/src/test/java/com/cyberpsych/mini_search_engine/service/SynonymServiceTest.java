package com.cyberpsych.mini_search_engine.service;

import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.entities.PageRankEntity;
import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.entities.TermFrequencyEntity;
import com.cyberpsych.mini_search_engine.repositories.PageRankRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import com.cyberpsych.mini_search_engine.repositories.TermFrequencyRepository;
import com.cyberpsych.mini_search_engine.services.SearchService;
import com.cyberpsych.mini_search_engine.services.SynonymService;
import com.cyberpsych.mini_search_engine.services.TextProcessorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SynonymServiceTest {

    @Autowired
    private SearchService searchService;

    @MockitoBean
    private SynonymService synonymService;

    @MockitoBean
    private TextProcessorService textProcessorService;

    @MockitoBean
    private PostingRepository postingRepository;

    @MockitoBean
    private PageRepository pageRepository;

    @MockitoBean
    private TermFrequencyRepository termFrequencyRepository;

    @MockitoBean
    private PageRankRepository pageRankRepository;

    @Test
    void shouldExpandQueryWithSynonyms() {
        when(textProcessorService.processText("quick")).thenReturn(List.of("quick"));
        when(textProcessorService.processText("fast")).thenReturn(List.of("fast"));
        when(synonymService.getSynonyms("quick")).thenReturn(List.of("fast"));

        Page page1 = new Page();
        page1.setId(1L);
        page1.setUrl("https://example.com");
        when(pageRepository.count()).thenReturn(1L);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page1));

        // Mock postings
        PostingEntity posting1 = new PostingEntity();
        posting1.setTerm("quick");
        posting1.setPageId(1L);
        posting1.setPositions(List.of(1));

        PostingEntity posting2 = new PostingEntity();
        posting2.setTerm("fast");
        posting2.setPageId(1L);
        posting2.setPositions(List.of(2));

        when(postingRepository.findByTerm("quick")).thenReturn(List.of(posting1));
        when(postingRepository.findByTerm("fast")).thenReturn(List.of(posting2));

        // Mock TF
        TermFrequencyEntity tfQuick = new TermFrequencyEntity();
        tfQuick.setTerm("quick");
        tfQuick.setDocumentFrequency(1L);
        TermFrequencyEntity tfFast = new TermFrequencyEntity();
        tfFast.setTerm("fast");
        tfFast.setDocumentFrequency(1L);

        when(termFrequencyRepository.findById("quick")).thenReturn(Optional.of(tfQuick));
        when(termFrequencyRepository.findById("fast")).thenReturn(Optional.of(tfFast));

        // Mock PageRank
        PageRankEntity prEntity = new PageRankEntity();
        prEntity.setPageId(1L);
        prEntity.setPageRankScore(0.5);
        when(pageRankRepository.findById(1L)).thenReturn(Optional.of(prEntity));

        // Search
        List<Map<String, Object>> results = searchService.search("quick");

        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("url")).isEqualTo("https://example.com");
        assertThat((Double) results.get(0).get("score")).isGreaterThanOrEqualTo(0.0);
    }
}
