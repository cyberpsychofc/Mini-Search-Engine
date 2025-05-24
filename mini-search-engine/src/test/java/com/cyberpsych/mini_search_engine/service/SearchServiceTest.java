package com.cyberpsych.mini_search_engine.service;

import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.entities.TermFrequencyEntity;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import com.cyberpsych.mini_search_engine.repositories.TermFrequencyRepository;
import com.cyberpsych.mini_search_engine.services.SearchService;
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
public class SearchServiceTest {
    @Autowired
    private SearchService searchService;

    @MockitoBean
    private TextProcessorService textProcessorService;

    @MockitoBean
    private PostingRepository postingRepository;

    @MockitoBean
    private PageRepository pageRepository;

    @MockitoBean
    private TermFrequencyRepository termFrequencyRepository;

    @Test
    void shouldRankResultWithTF_IDF(){
        when(textProcessorService.processText("quick")).thenReturn(List.of("quick"));

        Page page1 = new Page();
        page1.setId(1L);
        page1.setUrl("https://example.com");

        Page page2 = new Page();
        page2.setId(2L);
        page2.setUrl("https://example.org");

        when(pageRepository.count()).thenReturn(2L);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page1));
        when(pageRepository.findById(2L)).thenReturn(Optional.of(page2));

        //Mock postings
        PostingEntity posting1 = new PostingEntity();
        posting1.setTerm("quick");
        posting1.setPageId(1L);
        posting1.setPositions(List.of(1, 2, 3));
        PostingEntity posting2 = new PostingEntity();
        posting2.setPageId(2L);
        posting2.setPositions(List.of(1));
        when(postingRepository.findByTerm("quick")).thenReturn(List.of(posting1, posting2));

        // Mock TF
        TermFrequencyEntity tfEntity = new TermFrequencyEntity();
        tfEntity.setTerm("quick");
        tfEntity.setDocumentFrequency(1L); // to align with IDF > 0
        when(termFrequencyRepository.findById("quick")).thenReturn(Optional.of(tfEntity));

        // Perform search
        List<Map<String, Object>> results = searchService.search("quick");

        // verify
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("url")).isEqualTo("https://example.com");
        assertThat((Double) results.get(0).get("score")).isGreaterThan((Double) results.get(1).get("score"));
        assertThat(results.get(1).get("url")).isEqualTo("https://example.org");
    }
}
