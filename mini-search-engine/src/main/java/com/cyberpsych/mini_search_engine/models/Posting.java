package com.cyberpsych.mini_search_engine.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Posting {
    private Long pageId;
    private List<Integer> positions;

    public Posting(Long pageId, List<Integer> positions){
        this.pageId = pageId;
        this.positions = positions;
    }
}