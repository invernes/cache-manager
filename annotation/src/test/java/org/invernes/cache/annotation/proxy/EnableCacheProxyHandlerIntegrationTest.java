package org.invernes.cache.annotation.proxy;

import ch.qos.logback.classic.Level;
import org.invernes.cache.common.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Import(TestConfig.class)
@SpringBootTest(classes = EnableCacheProxyHandlerIntegrationTest.class)
@DisplayName("Интеграционные тесты для EnableCacheProxyHandler")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
public class EnableCacheProxyHandlerIntegrationTest {

    @Test
    @DisplayName("Тест методов с аннотациями с savePolicy = Cache.SavePolicy.NOT_NULL и Cache.SavePolicy.NOT_EMPTY")
    void invokeValidTest(ApplicationContext applicationContext) {
        String expectedLogMessage = "Value %s was cached with key %s";
        var logger = TestHelper.getLogger(EnableCacheProxyHandler.class, Level.DEBUG);
        logger.start();

        var proxy = applicationContext.getBean(TestConfig.TestEnableCacheAnnotatedInterface.class);
        var valueToCache = TestHelper.randomFromUuid();
        var expectedNotNull = proxy.testMethodNotNull(valueToCache);
        var actualNotNull = proxy.testMethodNotNull(TestHelper.randomFromUuid());
        assertEquals(expectedNotNull, actualNotNull);

        List<String> listToCache = List.of(TestHelper.randomFromUuid());
        List<String> anotherList = List.of(TestHelper.randomFromUuid(), TestHelper.randomFromUuid());
        var expectedNotEmpty = proxy.testMethodNotEmpty(listToCache);
        var actualNotEmpty = proxy.testMethodNotEmpty(anotherList);
        assertEquals(expectedNotEmpty, actualNotEmpty);

        logger.stop();
        assertEquals(2, logger.list.size());
        assertEquals(Level.DEBUG, logger.list.get(0).getLevel());
        assertEquals(String.format(expectedLogMessage, valueToCache, "testMethodNotNull"), logger.list.get(0).getFormattedMessage());
        assertEquals(Level.DEBUG, logger.list.get(1).getLevel());
        assertEquals(String.format(expectedLogMessage, listToCache, "testMethodNotEmpty"), logger.list.get(1).getFormattedMessage());
    }

    @Test
    @DisplayName("Тест метода с аннотацией с savePolicy = Cache.SavePolicy.NOT_EMPTY при типе возвращаемого значения не Collection<?>")
    void invokeSavePolicyNotEmptyResultIsNotCollection(ApplicationContext applicationContext) {
        String expectedLogMessage = "SavePolicy.NOT_EMPTY is not applicable to return type java.lang.String. Processing method without caching";
        var logger = TestHelper.getLogger(EnableCacheProxyHandler.class, Level.WARN);
        logger.start();

        var proxy = applicationContext.getBean(TestConfig.TestEnableCacheAnnotatedInterface.class);
        var expectedNotNull = proxy.testMethodNotEmpty(TestHelper.randomFromUuid());
        var actualNotNull = proxy.testMethodNotEmpty(TestHelper.randomFromUuid());

        assertNotEquals(expectedNotNull, actualNotNull);

        logger.stop();
        assertEquals(2, logger.list.size());
        assertEquals(Level.WARN, logger.list.get(0).getLevel());
        assertEquals(expectedLogMessage, logger.list.get(0).getFormattedMessage());
        assertEquals(Level.WARN, logger.list.get(1).getLevel());
        assertEquals(expectedLogMessage, logger.list.get(1).getFormattedMessage());
    }
}