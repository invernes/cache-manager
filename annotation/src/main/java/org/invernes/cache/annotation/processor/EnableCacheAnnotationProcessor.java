package org.invernes.cache.annotation.processor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.invernes.cache.annotation.Cache;
import org.invernes.cache.annotation.EnableCache;
import org.invernes.cache.manager.CacheManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EnableCacheAnnotationProcessor implements BeanFactoryPostProcessor {

    private Class<?>[] beanInterfaces;
    private ClassLoader beanClassLoader;
    private CacheManager cacheManager;
    private final Map<String, Cache> annotationMap = new HashMap<>();
    private  ConfigurableListableBeanFactory beanFactory;
    private String proxiedBeanName;
    private final Supplier<?> createProxy = () -> Proxy.newProxyInstance(
            beanClassLoader,
            beanInterfaces,
            new ProxyHandler(cacheManager, annotationMap, beanFactory, proxiedBeanName)
    );

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        cacheManager = beanFactory.getBean(CacheManager.class);
        Map<String, Object> enableCacheAnnotatedBeans = beanFactory.getBeansWithAnnotation(EnableCache.class);
        for (String beanName : enableCacheAnnotatedBeans.keySet()) {
            beanInterfaces = beanFactory.getType(beanName).getInterfaces();
            beanClassLoader = beanFactory.getBeanClassLoader();
            var bean = enableCacheAnnotatedBeans.get(beanName);
            for (Method method : bean.getClass().getMethods()) {
                annotationMap.put(method.getName(), method.getAnnotation(Cache.class));
            }

            proxiedBeanName = beanName + "Proxied";
            RootBeanDefinition proxyBeanDefinition = new RootBeanDefinition();
            proxyBeanDefinition.setBeanClass(beanFactory.getType(beanName));
            proxyBeanDefinition.setInstanceSupplier(createProxy);
            proxyBeanDefinition.setPrimary(true);

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            registry.registerBeanDefinition(proxiedBeanName, beanFactory.getBeanDefinition(beanName));
            registry.removeBeanDefinition(beanName);
            registry.registerBeanDefinition(beanName, proxyBeanDefinition);
        }
    }

    @RequiredArgsConstructor
    private static class ProxyHandler implements InvocationHandler {

        private final CacheManager cacheManager;
        private final Map<String, Cache> annotationMap;
        private final ConfigurableListableBeanFactory beanFactory;
        private final String proxiedBeanName;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
//            var cacheAnnotation = method.getAnnotation(Cache.class);
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
}
