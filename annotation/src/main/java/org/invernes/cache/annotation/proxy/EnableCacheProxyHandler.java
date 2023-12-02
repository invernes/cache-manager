package org.invernes.cache.annotation.proxy;

import lombok.RequiredArgsConstructor;
import org.invernes.cache.annotation.Cache;
import org.invernes.cache.manager.CacheManager;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class EnableCacheProxyHandler implements InvocationHandler {

    private final CacheManager cacheManager;
    private final Map<String, Cache> annotationMap;
    private final ConfigurableListableBeanFactory beanFactory;
    private final String proxiedBeanName;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        var proxied = beanFactory.getBean(proxiedBeanName);
        var cacheAnnotation = annotationMap.get(method.getName());
        String key = cacheAnnotation.name();
        Cache.SavePolicy savePolicy = cacheAnnotation.savePolicy();
        if (cacheManager.get(key) != null) {
            return cacheManager.get(cacheAnnotation.name());
        }
        var result = method.invoke(proxied, args);
        if (savePolicy == Cache.SavePolicy.NOT_EMPTY) {
            if (!((Collection<?>) result).isEmpty()) {
                cacheManager.save(key, result);
            }
        } else if (savePolicy == Cache.SavePolicy.NOT_NULL) {
            if (result != null) {
                cacheManager.save(key, result);
            }
        }
        return result;
    }
}