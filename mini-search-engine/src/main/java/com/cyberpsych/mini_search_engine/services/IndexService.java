package com.cyberpsych.mini_search_engine.services;

import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.entities.TermFrequencyEntity;
import com.cyberpsych.mini_search_engine.models.Posting;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import com.cyberpsych.mini_search_engine.repositories.TermFrequencyRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IndexService {
    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
    private final PostingRepository postingRepository;

    @Value("${crawler.user-agent}")
    private String userAgent;
    private final Map<String, List<Posting>> invertedIndex = new HashMap<>();
    private final PageRepository pageRepository;
    private final TermFrequencyRepository termFrequencyRepository;
    private final TextProcessorService textProcessorService;

    public IndexService(PageRepository pageRepository, TermFrequencyRepository termFrequencyRepository, TextProcessorService textProcessorService, PostingRepository postingRepository){
        this.pageRepository = pageRepository;
        this.termFrequencyRepository = termFrequencyRepository;
        this.textProcessorService = textProcessorService;
        this.postingRepository = postingRepository;
    }
    public void addPosting(String term, Posting posting){
        invertedIndex.computeIfAbsent(term, k-> new ArrayList<>()).add(posting);
        logger.debug("Add posting for term '{}': pageId = {}, positions = {}",
                term,
                posting.getPageId(),
                posting.getPositions()
        );
    }
    public List<Posting> getPostings(String term){
        return invertedIndex.getOrDefault(term, new ArrayList<>());
    }
    public void clearIndex(){
        invertedIndex.clear();
        logger.info("Inverted index cleared.");
    }

    @Scheduled(fixedDelay = 10800000)  // Set larger value in production
    @Transactional
    public void buildIndex(){
        logger.info("Starting Inverted Index Build");
        clearIndex();
        postingRepository.deleteAll();
        termFrequencyRepository.deleteAll();

        List<Page> pages = pageRepository.findAll();
        List<PostingEntity> postingsToSave = new ArrayList<>();
        Map<String, Long> termDocCounts = new HashMap<>();

        for (Page page: pages){
            try {
                Document doc = Jsoup.connect(page.getUrl())
                        .userAgent(userAgent)
                        .timeout(5000)
                        .get();

                List<String> tokens = textProcessorService.processText(doc.html());

                Map<String, List<Integer>> termPositions = new HashMap<>();
                for (int i=0; i < tokens.size(); i++){
                    String term = tokens.get(i);
                    if (term.length() > 255)
                        continue;
                    termPositions.computeIfAbsent(
                            term,
                            k -> new ArrayList<>()
                    ).add(i + 1);
                    termDocCounts.merge(term, 1L, (a, b) -> a);
                }

                for (Map.Entry<String, List<Integer>> entry : termPositions.entrySet()){
                    String term = entry.getKey();
                    List<Integer> positions = entry.getValue();

                    //In-memory index
                    Posting posting = new Posting(page.getId(), positions);
                    addPosting(term, posting);

                    //Persistent posting
                    PostingEntity postingEntity = new PostingEntity();
                    postingEntity.setTerm(term);
                    postingEntity.setPageId(page.getId());
                    postingEntity.setPositions(positions);
                    postingsToSave.add(postingEntity);
                }
            }
            catch (IOException e){
                logger.error("Error indexing page {}: {}", page.getUrl(), e.getMessage());
            }
        }
        postingRepository.saveAll(postingsToSave);

        List<TermFrequencyEntity> termFrequencies = termDocCounts.entrySet().stream()
                        .map(entry -> {
                            TermFrequencyEntity tf = new TermFrequencyEntity();
                            tf.setTerm(entry.getKey());
                            tf.setDocumentFrequency(entry.getValue());
                            return tf;
                        })
                .collect(Collectors.toList());
        termFrequencyRepository.saveAll(termFrequencies);
        logger.info("Inverted index built with {} terms, saved {} postings, {} term frequencies", invertedIndex.size(), postingsToSave.size(), termFrequencies.size());
    }
}
