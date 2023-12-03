package org.invernes.cache.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.experimental.UtilityClass;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@UtilityClass
public class TestHelper {

    public String randomFromUuid() {
        return UUID.randomUUID().toString();
    }

    public ListAppender<ILoggingEvent> getLogger(Class<?> clazz, Level level) {
        ListAppender<ILoggingEvent> logWatcher = new ListAppender<>();
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        logger.setLevel(level);
        logger.addAppender(logWatcher);
        return logWatcher;
    }

    public ListAppender<ILoggingEvent> getLogger(Class<?> clazz) {
        return getLogger(clazz, Level.ERROR);
    }
}