package com.cyberpsych.mini_search_engine.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class PostingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String term;

    private Long pageId;

    @ElementCollection
    private List<Integer> positions = new ArrayList<>();
}
