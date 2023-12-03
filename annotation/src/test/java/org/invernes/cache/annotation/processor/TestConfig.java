package org.invernes.cache.annotation.processor;

import org.invernes.cache.annotation.Cache;
import org.invernes.cache.manager.CacheManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.invernes.cache.annotation.EnableCache;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }

    @Bean
    public TestEnableCacheAnnotatedClass testEnableCacheAnnotatedClass() {
        return new TestEnableCacheAnnotatedClass();
    }

    @Bean
    public TestEnableCacheAnnotatedClassWithDuplicateCacheNames testEnableCacheAnnotatedClassWithDuplicateCacheNames() {
        return new TestEnableCacheAnnotatedClassWithDuplicateCacheNames();
    }

    @EnableCache
    public static class TestEnableCacheAnnotatedClass implements TestEnableCacheAnnotatedInterface {

        @Override
        @Cache(name = "cache1", savePolicy = Cache.SavePolicy.NOT_NULL)
        public Object testMethod1(Object o) {
            return o;
        }

        @Override
        @Cache(name = "cache2", savePolicy = Cache.SavePolicy.NOT_NULL)
        public Object testMethod2(Object o) {
            return o;
        }

    }

    @EnableCache
    public static class TestEnableCacheAnnotatedClassWithDuplicateCacheNames implements TestEnableCacheAnnotatedInterface {

        @Override
        @Cache(name = "cache1", savePolicy = Cache.SavePolicy.NOT_NULL)
        public Object testMethod1(Object o) {
            return o;
        }

        @Override
        @Cache(name = "cache1", savePolicy = Cache.SavePolicy.NOT_NULL)
        public Object testMethod2(Object o) {
            return o;
        }

    }

    public interface TestEnableCacheAnnotatedInterface {

        Object testMethod1(Object o);

        Object testMethod2(Object o);
    }
}
