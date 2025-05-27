package com.cyberpsych.mini_search_engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class MiniSearchEngineApplicationTests {

	@Test
	void contextLoads() {
	}

	// Load .env for testing
	@BeforeAll
	static void loadEnv() throws IOException {
		Files.lines(Paths.get(".env"))
				.filter(line -> line.contains("=") && !line.startsWith("#"))
				.forEach(line -> {
					String[] parts = line.split("=", 2);
					System.setProperty(parts[0], parts[1]);
				});
	}
}
