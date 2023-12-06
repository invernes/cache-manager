package org.invernes.cache.annotation.processor;

import org.invernes.cache.annotation.Cache;
import org.invernes.cache.manager.CacheManager;
import org.invernes.cache.manager.SimpleCacheManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.invernes.cache.annotation.EnableCache;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@TestConfiguration
public class TestConfig {

    @Bean
    public CacheManager cacheManager() {
        return new SimpleCacheManager();
    }

    @Bean
    public TestEnableCacheAnnotatedClass testEnableCacheAnnotatedClass() {
        return new TestEnableCacheAnnotatedClass();
    }

    @Bean
    public TestEnableCacheAnnotatedClassWithDuplicateCacheNames testEnableCacheAnnotatedClassWithDuplicateCacheNames() {
        return new TestEnableCacheAnnotatedClassWithDuplicateCacheNames();
    }

    @Bean
    public TestEnableCacheAnnotatedClassWithInvalidCacheManager testEnableCacheAnnotatedClassWithInvalidCacheManager() {
        return new TestEnableCacheAnnotatedClassWithInvalidCacheManager();
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

    @EnableCache(cacheManagerClass = FailCacheManager.class)
    public static class TestEnableCacheAnnotatedClassWithInvalidCacheManager implements TestEnableCacheAnnotatedInterface {

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

    public static class FailCacheManager implements CacheManager {

        public FailCacheManager(String a) {} // no no args constructor

        private final Map<String, Object> objectMap = new HashMap<>();

        public Object get(String key) {
            return objectMap.getOrDefault(key, null);
        }

        public void save(String key, Object value) {
            objectMap.put(key, value);
        }

        public boolean containsKey(String key) {
            return objectMap.containsKey(key);
        }

        public Set<String> keySet() {
            return objectMap.keySet();
        }
    }
}
