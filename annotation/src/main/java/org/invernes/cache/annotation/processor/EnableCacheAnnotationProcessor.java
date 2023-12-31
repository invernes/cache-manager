package org.invernes.cache.annotation.processor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.invernes.cache.annotation.Cache;
import org.invernes.cache.annotation.EnableCache;
import org.invernes.cache.annotation.proxy.EnableCacheProxyHandler;
import org.invernes.cache.exception.CacheManagerException;
import org.invernes.cache.manager.CacheManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class EnableCacheAnnotationProcessor implements BeanFactoryPostProcessor {
    private static final String PROXIED_BEAN_SUFFIX = "Proxied";
    private static final String FAILED_TO_CREATE_CACHE_MANAGER_INSTANCE =
            "Failed to create cache manager instance";

    @Override
    @SuppressWarnings("ConstantConditions")
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, Object> enableCacheAnnotatedBeans = beanFactory.getBeansWithAnnotation(EnableCache.class);
        if (enableCacheAnnotatedBeans.values().isEmpty()) {
            log.info("No beans with @EnableCache annotation found");
            return;
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        var cacheManagerBeans = beanFactory.getBeansOfType(CacheManager.class);

        for (String beanName : enableCacheAnnotatedBeans.keySet()) {
            EnableCache enableCacheAnnotation = beanFactory.findAnnotationOnBean(beanName, EnableCache.class);
            Class<?> cacheManagerClass = enableCacheAnnotation.cacheManagerClass();
            if (cacheManagerBeans.values().isEmpty() && enableCacheAnnotation != null) {
                BeanDefinition cacheManagerBeanDefinition;
                cacheManagerBeanDefinition = createBeanDefinitionForType(cacheManagerClass, () -> {
                    try {
                        return cacheManagerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new CacheManagerException(FAILED_TO_CREATE_CACHE_MANAGER_INSTANCE, e);
                    }
                });
                registry.registerBeanDefinition("cacheManager", cacheManagerBeanDefinition);
            }
            CacheManager cacheManager = (CacheManager) beanFactory.getBean(cacheManagerClass);

            Class<?> beanClass = beanFactory.getType(beanName);
            var bean = enableCacheAnnotatedBeans.get(beanName);
            Class<?>[] beanInterfaces = beanClass.getInterfaces();
            ClassLoader beanClassLoader = beanFactory.getBeanClassLoader();
            Map<String, Cache> annotationMap = getObjectMethodsAnnotations(bean);
            validateMethodsAnnotations(annotationMap);

            // copy beanDefinition and create copy with different name
            String proxiedBeanName = beanName + PROXIED_BEAN_SUFFIX;
            removeBeanDefinitionAndCreateCopyWithDifferentName(registry, beanName, proxiedBeanName);

            // register proxy with original bean name
            Supplier<?> beanInstanceSupplier = () -> Proxy.newProxyInstance(
                    beanClassLoader,
                    beanInterfaces,
                    new EnableCacheProxyHandler(cacheManager, annotationMap, bean)
            );
            BeanDefinition proxyBeanDefinition = createBeanDefinitionForType(beanClass, beanInstanceSupplier);
            registry.registerBeanDefinition(beanName, proxyBeanDefinition);
        }
    }

    private BeanDefinition createBeanDefinitionForType(Class<?> beanClass, Supplier<?> beanInstanceSupplier) {
        log.debug("Creating beanDefinition for type [{}]", beanClass.getName());
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setInstanceSupplier(beanInstanceSupplier);
        beanDefinition.setPrimary(true);
        return beanDefinition;
    }

    private Map<String, Cache> getObjectMethodsAnnotations(Object object) {
        Map<String, Cache> annotationMap = new HashMap<>();
        for (Method method : object.getClass().getMethods()) {
            annotationMap.put(method.getName(), method.getAnnotation(Cache.class));
        }
        return annotationMap;
    }

    private void validateMethodsAnnotations(Map<String, Cache> annotationMap) {
        Set<String> cacheNames = new HashSet<>();
        Set<String> duplicateCacheNames = new HashSet<>();
        for (String cacheName : annotationMap.values().stream().filter(Objects::nonNull).map(Cache::name).toList()) {
            if (cacheNames.contains(cacheName)) {
                duplicateCacheNames.add(cacheName);
            }
            cacheNames.add(cacheName);
        }
        if (!duplicateCacheNames.isEmpty()) {
            String errorMessage = "Duplicate cache names found: " + String.join(",", duplicateCacheNames);
            throw new CacheManagerException(errorMessage);
        }
    }

    private void removeBeanDefinitionAndCreateCopyWithDifferentName(BeanDefinitionRegistry registry,
                                                                    String beanName,
                                                                    String newBeanName) {
        log.debug("Recreating beanDefinition for name [{}] with new name [{}]", beanName, newBeanName);
        registry.registerBeanDefinition(newBeanName, registry.getBeanDefinition(beanName));
        registry.removeBeanDefinition(beanName);
    }
}