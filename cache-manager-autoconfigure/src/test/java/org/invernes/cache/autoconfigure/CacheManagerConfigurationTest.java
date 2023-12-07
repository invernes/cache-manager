package org.invernes.cache.autoconfigure;

import org.invernes.cache.annotation.processor.EnableCacheAnnotationProcessor;
import org.invernes.cache.manager.CacheManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тест поднятия контекста")
@SpringBootTest(classes = CacheManagerConfiguration.class)
class CacheManagerConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextTest() {
        assertNotNull(applicationContext);

        var enableCacheAnnotationProcessor = applicationContext.getBean(EnableCacheAnnotationProcessor.class);
        assertNotNull(enableCacheAnnotationProcessor);

        var cacheManagerBeans = applicationContext.getBeansOfType(CacheManager.class);
        cacheManagerBeans.values().forEach(Assertions::assertNull);
    }
}