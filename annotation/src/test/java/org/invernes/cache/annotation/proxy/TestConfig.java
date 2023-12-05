package org.invernes.cache.annotation.proxy;

import lombok.Data;
import org.invernes.cache.annotation.Cache;
import org.invernes.cache.annotation.EnableCache;
import org.invernes.cache.annotation.processor.EnableCacheAnnotationProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public EnableCacheAnnotationProcessor enableCacheAnnotationProcessor() {
        return new EnableCacheAnnotationProcessor();
    }

    @Bean
    public TestEnableCacheAnnotatedClass testEnableCacheAnnotatedClass() {
        return new TestEnableCacheAnnotatedClass();
    }

    public interface TestEnableCacheAnnotatedInterface {

        Object testMethodNotNull(Object o);

        Object testMethodNotEmpty(Object o);
    }

    @Data
    @EnableCache
    public static class TestEnableCacheAnnotatedClass implements TestEnableCacheAnnotatedInterface {

        @Override
        @Cache(name = "testMethodNotNull", savePolicy = Cache.SavePolicy.NOT_NULL)
        public Object testMethodNotNull(Object o) {
            return o;
        }

        @Override
        @Cache(name = "testMethodNotEmpty", savePolicy = Cache.SavePolicy.NOT_EMPTY)
        public Object testMethodNotEmpty(Object o) {
            return o;
        }
    }
}