package org.invernes.cache.autoconfigure;

import org.invernes.cache.annotation.processor.EnableCacheAnnotationProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CacheManagerConfiguration {

    @Bean
    public EnableCacheAnnotationProcessor enableCacheAnnotationProcessor() {
        return new EnableCacheAnnotationProcessor();
    }
}
