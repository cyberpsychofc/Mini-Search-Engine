package com.cyberpsych.mini_search_engine.service;

import com.cyberpsych.mini_search_engine.models.Posting;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.services.IndexService;
import com.cyberpsych.mini_search_engine.services.TextProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
public class IndexServiceTest {

    @Autowired
    private IndexService indexService;
    @MockitoBean
    private PageRepository pageRepository;
    @MockitoBean
    private TextProcessorService textProcessorService;

    @BeforeEach
    void setUp(){
        indexService.clearIndex();
    }
    @Test
    public void shouldAddAndRetrievePostings(){
        Posting posting = new Posting(1L, List.of(1,3));
        indexService.addPosting("test", posting);

        List<Posting> postings = indexService.getPostings("test");
        assertThat(postings).hasSize(1);
        assertThat(postings.get(0).getPageId()).isEqualTo(1L);
        assertThat(postings.get(0).getPositions()).containsExactly(1,3);
        assertThat(indexService.getPostings("unknown")).isEmpty();
    }
}
