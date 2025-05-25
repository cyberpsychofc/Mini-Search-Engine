package com.cyberpsych.mini_search_engine.service;

import com.cyberpsych.mini_search_engine.entities.Link;
import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.entities.PageRankEntity;
import com.cyberpsych.mini_search_engine.repositories.LinkRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRankRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.services.PageRankService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PageRankServiceTest {
    @Autowired
    private PageRankService pageRankService;

    @MockitoBean
    private PageRepository pageRepository;

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private PageRankRepository pageRankRepository;

    @Captor
    private ArgumentCaptor<List<PageRankEntity>> prCaptor;

    @Test
    void shouldCalculatePageRank() {
        Page page1 = new Page();
        page1.setId(1L);
        //page1.setUrl("https://example.com");
        Page page2 = new Page();
        page2.setId(2L);
        //page2.setUrl("https://example.org");
        when(pageRepository.findAll()).thenReturn(List.of(page1, page2));

        Link link = new Link();
        link.setFrom(page1);
        link.setTo(page2);
        when(linkRepository.findAll()).thenReturn(List.of(link));

        //calc rank
        pageRankService.calculatePageRank();

        // verify saveAll was called exactly once
        verify(pageRankRepository).saveAll(prCaptor.capture());

        List<PageRankEntity> savedEntities = prCaptor.getValue();

        assertThat(savedEntities).hasSize(2);

        PageRankEntity pr1 = savedEntities.stream()
                .filter(pr -> pr.getPageId().equals(1L))
                .findFirst()
                .orElse(null);
        PageRankEntity pr2 = savedEntities.stream()
                .filter(pr -> pr.getPageId().equals(2L))
                .findFirst()
                .orElse(null);

        assertThat(pr1).isNotNull();
        assertThat(pr2).isNotNull();
        assertThat(pr2.getPageRankScore()).isGreaterThan(pr1.getPageRankScore());
    }
}
