package org.invernes.cache.annotation.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.invernes.cache.annotation.Cache;
import org.invernes.cache.manager.CacheManager;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class EnableCacheProxyHandler implements InvocationHandler {

    private static final String INVALID_SAVE_POLICY_LOG_MESSAGE =
            "SavePolicy.NOT_EMPTY is not applicable to return type {}. Processing method without caching";
    private static final String RESULT_SAVED_LOG_MESSAGE = "Value {} was cached with key {}";

    private final CacheManager cacheManager;
    private final Map<String, Cache> annotationMap;
    private final Object original;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        var cacheAnnotation = annotationMap.get(method.getName());
        String key = cacheAnnotation.name();
        Cache.SavePolicy savePolicy = cacheAnnotation.savePolicy();
        if (cacheManager.get(key) != null) {
            return cacheManager.get(cacheAnnotation.name());
        }
        var result = method.invoke(original, args);
        if (savePolicy == Cache.SavePolicy.NOT_EMPTY) {
            if (!(result instanceof Collection)) {
                log.warn(INVALID_SAVE_POLICY_LOG_MESSAGE, result.getClass().getName());
                return result;
            }
            if (!CollectionUtils.isEmpty((Collection<?>) result)) {
                cacheManager.save(key, result);
                log.debug(RESULT_SAVED_LOG_MESSAGE, result, key);
            }
        } else if (savePolicy == Cache.SavePolicy.NOT_NULL) {
            if (result != null) {
                cacheManager.save(key, result);
                log.debug(RESULT_SAVED_LOG_MESSAGE, result, key);
            }
        }
        return result;
    }
}