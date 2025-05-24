package com.cyberpsych.mini_search_engine.repositories;

import com.cyberpsych.mini_search_engine.entities.TermFrequencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermFrequencyRepository extends JpaRepository<TermFrequencyEntity, String> {
}
