package com.intuit.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager; // Inject the CacheManager bean

    @Test
    void testCacheManagerBeanCreated() {
        // Assert that the cacheManager bean is injected and not null
        assertNotNull(cacheManager, "CacheManager bean should be created");

        // Assert that the CacheManager is of type ConcurrentMapCacheManager
        assertTrue(cacheManager instanceof ConcurrentMapCacheManager, "CacheManager should be an instance of ConcurrentMapCacheManager");

        // Assert that the cache named "usercache" is present
        assertNotNull(cacheManager.getCache("usercache"), "Cache 'usercache' should be created and available");
    }
}
