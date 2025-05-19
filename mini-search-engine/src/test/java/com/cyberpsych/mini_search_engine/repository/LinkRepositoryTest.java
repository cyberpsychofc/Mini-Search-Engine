package com.cyberpsych.mini_search_engine.repository;

import com.cyberpsych.mini_search_engine.entities.Link;
import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.repositories.LinkRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LinkRepositoryTest {

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LinkRepository linkRepository;

    @Test
    public void shouldSaveAndFindLink(){
        Page fromPage = new Page();
        fromPage.setUrl("https://example.com");
        Page toPage = new Page();
        toPage.setUrl("https://example.org");
        pageRepository.save(fromPage);
        pageRepository.save(toPage);

        //create link
        Link link = new Link();
        link.setFrom(fromPage);
        link.setTo(toPage);

        //save to db
        Link savedLink = linkRepository.save(link);

        //verify it was saved
        assertThat(savedLink.getId()).isNotNull();
        assertThat(savedLink.getFrom().getUrl()).isEqualTo("https://example.com");
        assertThat(savedLink.getTo().getUrl()).isEqualTo("https://example.org");

        //find by ID
        Link foundLink = linkRepository.findById(savedLink.getId()).orElse(null);
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.getFrom().getUrl()).isEqualTo("https://example.com");
    }
}
