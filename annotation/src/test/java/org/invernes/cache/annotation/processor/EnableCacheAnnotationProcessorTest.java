package org.invernes.cache.annotation.processor;

import ch.qos.logback.classic.Level;
import org.invernes.cache.common.TestHelper;
import org.invernes.cache.exception.CacheManagerException;
import org.invernes.cache.manager.CacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(TestConfig.class)
@DisplayName("Тесты класса EnableCacheAnnotationProcessor")
@SpringBootTest(classes = EnableCacheAnnotationProcessorTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EnableCacheAnnotationProcessorTest {

    private static final String PROXIED_BEAN_SUFFIX = "Proxied";
    private static final String BEAN_NAME = "testEnableCacheAnnotatedClass";
    private static final Class<?> BEAN_TYPE = TestConfig.TestEnableCacheAnnotatedClass.class;
    private static final String DUPLICATE_CACHES_BEAN_NAME = "testEnableCacheAnnotatedClassWithDuplicateCacheNames";
    private static final String CACHE_MANAGER_BEAN_NAME = "cacheManager";

    private final EnableCacheAnnotationProcessor sut = new EnableCacheAnnotationProcessor();

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Test
    @DisplayName("В контексте нет бина типа CacheManager, проксируемый объект содержит кеши с одинаковыми именами ")
    void noCacheManager_duplicateCacheNames() {
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(BEAN_NAME);
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(CACHE_MANAGER_BEAN_NAME);

        var logger = TestHelper.getLogger(EnableCacheAnnotationProcessor.class, Level.DEBUG);

        logger.start();
        assertThrows(CacheManagerException.class, () -> sut.postProcessBeanFactory(beanFactory));
        logger.stop();

        String expectedDebugMessage = String.format("Creating beanDefinition for type [%s]", CacheManager.class.getName());

        assertEquals(1, logger.list.size());

        assertEquals(Level.DEBUG, logger.list.get(0).getLevel());
        assertEquals(expectedDebugMessage, logger.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("В контексте есть бин типа CacheManager, проксируемый объект содержит валидные кеши")
    void validCaches() {
        ((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(DUPLICATE_CACHES_BEAN_NAME);

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