package com.cyberpsych.mini_search_engine.repositories;

import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<PostingEntity, Long> {
}
