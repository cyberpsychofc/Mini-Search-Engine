package com.cyberpsych.mini_search_engine.integration;

import com.cyberpsych.mini_search_engine.entities.Page;
import com.cyberpsych.mini_search_engine.entities.PostingEntity;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.repositories.PostingRepository;
import com.cyberpsych.mini_search_engine.services.IndexService;
import com.cyberpsych.mini_search_engine.services.WebCrawlerService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class SearchIntegrationTest {
    @Container
    public static final GenericContainer<?> h2Container = new GenericContainer<>("oscarfonts/h2") // no official h2 image available on dockerhub
            .withExposedPorts(1521)
            .withEnv("H2_OPTIONS", "-ifNotExists")
            .waitingFor(Wait.forListeningPort()); // w/o it might raise a timeout exception on slow machines

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebCrawlerService webCrawlerService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private PageRepository pageRepository;

    private MockWebServer mockWebServer;
    @Autowired
    private PostingRepository postingRepository;

    @DynamicPropertySource
    static void configureH2(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",() ->
                "jdbc:h2:tcp://localhost:" + h2Container.getMappedPort(1521) + "/mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        pageRepository.deleteAll();
        postingRepository.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCrawlIndexAndSearch() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("User-agent: *\nAllow: /")
                .addHeader("Content-Type", "text/plain"));

        String pageUrl = mockWebServer.url("/page1").toString();
        String html = """
                <html>
                    <body>
                        <p>The quick brown fox jumps.</p>
                        <a href="/page2">Link to page 2</a>
                    </body>
                </html>
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(html)
                .addHeader("Content-Type", "text/html"));

        String page2Url = mockWebServer.url("/page2").toString();
        String htmlpage2 = """
                <html>
                    <body>
                        <p>Quick fox runs.</p>
                    </body>
                </html>
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(htmlpage2)
                .addHeader("Content-Type", "text/html"));

        //integration
        webCrawlerService.crawl(pageUrl);
        indexService.buildIndex();

        // verify db transactions
        List<Page> pages = pageRepository.findAll();
        assertThat(pages).hasSize(1);
        assertThat(pages).extracting(Page::getUrl).contains(pageUrl); // add page2Url

        List<PostingEntity> postings = postingRepository.findAll();
        assertThat(postings).isNotEmpty();
        assertThat(postings).extracting(PostingEntity::getTerm).contains("quick", "fox");

        // search
        mockMvc.perform(get("/api/search?q=quick fox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value(pageUrl))
                .andExpect(jsonPath("$[0].score").exists());
                //.andExpect(jsonPath("$[1].url").value(page2Url))
                //.andExpect(jsonPath("$[1].score").exists());
    }
}
