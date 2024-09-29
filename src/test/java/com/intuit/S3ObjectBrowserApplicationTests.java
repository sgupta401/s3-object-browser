package com.intuit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class S3ObjectBrowserApplicationTests {

	@Autowired
	private CacheManager cacheManager;

	@Test
	public void contextLoads() {
		// Test that the context successfully loads
	}

	@Test
	public void cacheManagerIsAvailable() {
		// Ensure that the CacheManager is loaded and available
		assertThat(cacheManager).isNotNull();
	}
}
