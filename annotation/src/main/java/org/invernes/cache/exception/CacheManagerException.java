package org.invernes.cache.exception;

public class CacheManagerException extends RuntimeException {

    public CacheManagerException(String message) {
        super(message);
    }

    public CacheManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}