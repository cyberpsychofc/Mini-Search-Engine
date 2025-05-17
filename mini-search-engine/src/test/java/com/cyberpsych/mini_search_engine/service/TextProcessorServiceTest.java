package com.cyberpsych.mini_search_engine.service;

import com.cyberpsych.mini_search_engine.services.TextProcessorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TextProcessorServiceTest {

    @Autowired
    private TextProcessorService textProcessorService;

    @Test
    public void shouldProcessText(){
        String html = """
            <html>
            <body>
                <p>The quick brown fox is running over the lazy dog.</p>
            </body>
            </html>
            """;
        List<String> tokens = textProcessorService.processText(html);
        assertThat(tokens).containsExactly(
                "quick", "brown", "fox", "run", "lazi", "dog"
        );
    }
}
