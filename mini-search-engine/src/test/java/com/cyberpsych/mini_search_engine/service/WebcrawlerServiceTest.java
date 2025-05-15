package com.cyberpsych.mini_search_engine.service;

import com.cyberpsych.mini_search_engine.repositories.LinkRepository;
import com.cyberpsych.mini_search_engine.repositories.PageRepository;
import com.cyberpsych.mini_search_engine.services.WebCrawlerService;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@SpringBootTest
public class WebcrawlerServiceTest {

    @Autowired
    private WebCrawlerService webCrawlerService;
    @MockitoBean
    private PageRepository pageRepository;
    @MockitoBean
    private LinkRepository linkRepository;
    @MockitoBean
    private RateLimiter rateLimiter;

    private MockWebServer mockWebServer;
    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);


    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldExtractLinksFromPage() throws IOException {
        String html = """
            <html>
            <body>
                <a href="/page1">Page 1</a>
                <a href="https://example.org">External Page</a>
            </body>
            </html>
            """;
        mockWebServer.enqueue(new MockResponse().setBody(html).addHeader("Content-Type","text/html"));

        // extract links
        String testUrl = mockWebServer.url("/").toString();
        Set<String> links = webCrawlerService.extractLinks(testUrl);

        //verify
        assertThat(links).hasSize(2);
        assertThat(links).contains(
                testUrl + "page1",
                "https://example.org"
        );
    }
}
