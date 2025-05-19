package com.cyberpsych.mini_search_engine.repository;

import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PageRepositoryTest {

    @Autowired
    private PageRepository pageRepository;

    @Test
    public void shouldSaveAndFindPageById(){
        Page page = new Page();
        page.setUrl("https://example.com");

        //save to db
        Page savedPage = pageRepository.save(page);

        //verify if it was saved
        assertThat(savedPage.getId()).isNotNull();
        assertThat(savedPage.getUrl()).isEqualTo("https://example.com");

        //Find by ID
        Page foundPage = pageRepository.findById(savedPage.getId()).orElse(null);
        assertThat(foundPage).isNotNull();
        assertThat(foundPage.getUrl()).isEqualTo("https://example.com");
    }
}
