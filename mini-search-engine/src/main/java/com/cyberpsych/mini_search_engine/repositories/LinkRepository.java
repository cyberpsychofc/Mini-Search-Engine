package com.cyberpsych.mini_search_engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cyberpsych.mini_search_engine.entities.Link;

public interface LinkRepository extends JpaRepository<Link, Long> {
}