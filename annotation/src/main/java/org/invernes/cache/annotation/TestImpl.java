package org.invernes.cache.annotation;

@EnableCache
public class TestImpl implements Test {

    @Cache(name = "testMethod", savePolicy = Cache.SavePolicy.NOT_NULL)
    public Object testMethod(Object o) {
        return o;
    }
}