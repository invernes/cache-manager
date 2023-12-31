package org.invernes.cache.annotation.processor;

import ch.qos.logback.classic.Level;
import org.invernes.cache.common.TestHelper;
import org.invernes.cache.exception.CacheManagerException;
import org.invernes.cache.manager.SimpleCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestConfig.class)
@DisplayName("Тесты класса EnableCacheAnnotationProcessor")
@SpringBootTest(classes = EnableCacheAnnotationProcessorTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EnableCacheAnnotationProcessorTest {

    private static final String PROXIED_BEAN_SUFFIX = "Proxied";
    private static final String BEAN_NAME = "testEnableCacheAnnotatedClass";
    private static final Class<?> BEAN_TYPE = TestConfig.TestEnableCacheAnnotatedClass.class;
    private static final String DUPLICATE_CACHES_BEAN_NAME = "testEnableCacheAnnotatedClassWithDuplicateCacheNames";
    private static final String INVALID_CACHE_MANAGER_BEAN_NAME = "testEnableCacheAnnotatedClassWithInvalidCacheManager";
    private static final String CACHE_MANAGER_BEAN_NAME = "cacheManager";

    private final EnableCacheAnnotationProcessor sut = new EnableCacheAnnotationProcessor();

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Test
    @DisplayName("Нет бинов с аннотацией EnableCache")
    void noEnableCacheBeans() {
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(DUPLICATE_CACHES_BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(CACHE_MANAGER_BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(INVALID_CACHE_MANAGER_BEAN_NAME);

        var logger = TestHelper.getLogger(EnableCacheAnnotationProcessor.class, Level.INFO);

        logger.start();
        sut.postProcessBeanFactory(beanFactory);
        logger.stop();

        String expectedInfoMessage = "No beans with @EnableCache annotation found";

        assertEquals(1, logger.list.size());

        assertEquals(Level.INFO, logger.list.get(0).getLevel());
        assertEquals(expectedInfoMessage, logger.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("В контексте нет бина типа CacheManager, в аннотации указан cacheManager без подходящего конструктора")
    void noCacheManager_invalidCacheManagerProvided() {
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(CACHE_MANAGER_BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(DUPLICATE_CACHES_BEAN_NAME);

        Exception thrown = assertThrows(BeanCreationException.class, () -> sut.postProcessBeanFactory(beanFactory));
        assertEquals("Error creating bean with name 'cacheManager': Instantiation of supplied bean failed", thrown.getMessage());
        Throwable cause = thrown.getCause();
        assertTrue(cause instanceof CacheManagerException);
        assertEquals("Failed to create cache manager instance", cause.getMessage());
    }

    @Test
    @DisplayName("В контексте нет бина типа CacheManager, проксируемый объект содержит кеши с одинаковыми именами ")
    void noCacheManager_duplicateCacheNames() {
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(CACHE_MANAGER_BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(INVALID_CACHE_MANAGER_BEAN_NAME);

        var logger = TestHelper.getLogger(EnableCacheAnnotationProcessor.class, Level.DEBUG);

        logger.start();
        assertThrows(CacheManagerException.class, () -> sut.postProcessBeanFactory(beanFactory));
        logger.stop();

        String expectedDebugMessage = String.format("Creating beanDefinition for type [%s]", SimpleCacheManager.class.getName());

        assertEquals(1, logger.list.size());

        assertEquals(Level.DEBUG, logger.list.get(0).getLevel());
        assertEquals(expectedDebugMessage, logger.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("В контексте есть бин типа CacheManager, проксируемый объект содержит валидные кеши")
    void validCaches() {
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(DUPLICATE_CACHES_BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(INVALID_CACHE_MANAGER_BEAN_NAME);

        var logger = TestHelper.getLogger(EnableCacheAnnotationProcessor.class, Level.DEBUG);

        logger.start();
        sut.postProcessBeanFactory(beanFactory);
        logger.stop();

        String expectedDebugMessage1 = String.format(
                "Recreating beanDefinition for name [%s] with new name [%s]", BEAN_NAME, BEAN_NAME + PROXIED_BEAN_SUFFIX
        );
        String expectedDebugMessage2 = String.format("Creating beanDefinition for type [%s]", BEAN_TYPE.getName());

        assertEquals(2, logger.list.size());

        assertEquals(Level.DEBUG, logger.list.get(0).getLevel());
        assertEquals(expectedDebugMessage1, logger.list.get(0).getFormattedMessage());

        assertEquals(Level.DEBUG, logger.list.get(1).getLevel());
        assertEquals(expectedDebugMessage2, logger.list.get(1).getFormattedMessage());
    }
}