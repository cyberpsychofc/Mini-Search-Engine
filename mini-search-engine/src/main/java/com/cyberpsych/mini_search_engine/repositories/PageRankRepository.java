package com.cyberpsych.mini_search_engine.repositories;

import com.cyberpsych.mini_search_engine.entities.PageRankEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRankRepository extends JpaRepository<PageRankEntity, Long> {
}
