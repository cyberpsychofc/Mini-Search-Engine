package com.cyberpsych.mini_search_engine.services;

import com.cyberpsych.mini_search_engine.entities.Link;
import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.entities.PageRankEntity;
import com.cyberpsych.mini_search_engine.repositories.LinkRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRankRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PageRankService {
    private static final Logger logger = LoggerFactory.getLogger(PageRankService.class);
    private static final double DAMPING_FACTOR = 0.85;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;
    private static final int MAX_ITERATIONS = 100;

    private final PageRepository pageRepository;
    private final LinkRepository linkRepository;
    private final PageRankRepository pageRankRepository;

    public PageRankService(PageRepository pageRepository, LinkRepository linkRepository, PageRankRepository pageRankRepository){
        this.pageRepository = pageRepository;
        this.linkRepository = linkRepository;
        this.pageRankRepository = pageRankRepository;
    }

    @Scheduled(fixedDelay = 900000)
    @Transactional
    public void calculatePageRank() {
        logger.info("Starting PageRank calculation");

        List<Page> pages = pageRepository.findAll();
        if (pages.isEmpty()){
            logger.info("No pages to rank");
            return;
        }

        int numPages = pages.size();
        Map<Long, Double> pageRankScores = new HashMap<>();
        Map<Long, List<Long>> incomingLinks = new HashMap<>();
        Map<Long, Integer> outgoingLinkCounts = new HashMap<>();

        // Score initialization
        double initialScore = 1.0 / numPages;
        for (Page page : pages) {
            pageRankScores.put(page.getId(), initialScore);
            incomingLinks.put(page.getId(), new ArrayList<>());
            outgoingLinkCounts.put(page.getId(), 0);
        }

        // Link Graph
        List<Link> links = linkRepository.findAll();
        for (Link link : links) {
            Long fromId = link.getFrom().getId();
            Long toId = link.getTo().getId();
            incomingLinks.computeIfAbsent(toId, k -> new ArrayList<>()).add(fromId);
            outgoingLinkCounts.merge(fromId, 1, Integer::sum);
        }

        // Iteration until convergence
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++){
            Map<Long, Double> newScores = new HashMap<>();
            double maxChange = 0.0;

            for (Page page : pages) {
                Long pageId = page.getId();
                double sum = 0.0;
                for (Long inLink : incomingLinks.getOrDefault(pageId, List.of())){
                    Integer outLinks = outgoingLinkCounts.getOrDefault(inLink, 0);
                    if (outLinks > 0){
                        sum += pageRankScores.get(inLink);
                    }
                }

                double newScore = (1.0 - DAMPING_FACTOR) / numPages + DAMPING_FACTOR * sum;
                newScores.put(pageId, newScore);

                maxChange = Math.max(maxChange, Math.abs(newScore - pageRankScores.get(pageId)));
            }
            pageRankScores = newScores;
            if (maxChange < CONVERGENCE_THRESHOLD) {
                logger.info("Pagerank converged after {} iternations", iteration + 1);
                break;
            }
        }

        // save scores
        pageRankRepository.deleteAll();
        List<PageRankEntity> entities = pageRankScores.entrySet().stream()
                .map(entry -> {
                    PageRankEntity entity = new PageRankEntity();
                    entity.setPageId(entry.getKey());
                    entity.setPageRankScore(entry.getValue());
                    return entity;
                })
                .collect(Collectors.toList());
        pageRankRepository.saveAll(entities);

        logger.info("PageRank calculation completed, saved {} scores", entities.size());
    }
}
