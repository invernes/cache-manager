package org.invernes.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    String name() default "";
    SavePolicy savePolicy() default SavePolicy.NOT_NULL;

    enum SavePolicy {
        NOT_NULL, NOT_EMPTY
    }
}