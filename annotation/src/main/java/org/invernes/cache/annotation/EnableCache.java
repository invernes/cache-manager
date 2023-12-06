package org.invernes.cache.annotation;

import org.invernes.cache.manager.SimpleCacheManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableCache {
    Class<?> cacheManagerClass() default SimpleCacheManager.class;
}
