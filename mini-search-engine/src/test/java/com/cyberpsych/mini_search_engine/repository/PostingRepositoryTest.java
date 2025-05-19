package com.cyberpsych.mini_search_engine.repository;
import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@DataJpaTest
public class PostingRepositoryTest {

    @Autowired
    private PostingRepository postingRepository;

    @Test
    void shouldSaveAndFindPostingEntity(){
        PostingEntity posting = new PostingEntity();
        posting.setTerm("run");
        posting.setPageId(1L);
        posting.setPositions(List.of(1, 3));

        //saving to database
        PostingEntity savedPosting = postingRepository.save(posting);

        //Verifications
        assertThat(savedPosting.getId()).isNotNull();
        assertThat(savedPosting.getTerm()).isEqualTo("run");
        assertThat(savedPosting.getPageId()).isEqualTo(1L);
        assertThat(savedPosting.getPositions()).containsExactly(1, 3);

        // find by ID
        PostingEntity foundPosting = postingRepository.findById(savedPosting.getId()).orElse(null);
        assertThat(foundPosting).isNotNull();
        assertThat(foundPosting.getTerm()).isEqualTo("run");
    }
}
