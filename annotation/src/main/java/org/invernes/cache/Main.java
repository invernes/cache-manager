package org.invernes.cache;

import org.invernes.cache.annotation.Cache;
import org.invernes.cache.annotation.EnableCache;
import org.invernes.cache.annotation.Test;
import org.invernes.cache.annotation.TestImpl;
import org.invernes.cache.annotation.processor.EnableCacheAnnotationProcessor;
import org.invernes.cache.manager.CacheManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        var context = SpringApplication.run(Main.class, args);
        var cacheManager = context.getBean(CacheManager.class);
        cacheManager.keySet().forEach(System.out::println);

        var proxy = context.getBean(Test.class);
        System.out.println(proxy.testMethod("sdfsdf"));

        System.out.println(proxy.testMethod("asdads"));

    }
}

@Configuration
class Config {

    @Bean
    public TestImpl test() {
        return new TestImpl();
    }

    @Bean
    public CacheManager cacheManager() {
        return new CacheManager();
    }

    @Bean
    public EnableCacheAnnotationProcessor enableCacheAnnotationProcessor() {
        return new EnableCacheAnnotationProcessor();
    }
}

