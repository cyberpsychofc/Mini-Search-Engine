package com.cyberpsych.mini_search_engine.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TermFrequencyEntity {
    @Id
    private String term;

    private Long documentFrequency;
}
