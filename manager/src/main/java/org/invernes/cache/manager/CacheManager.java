package org.invernes.cache.manager;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CacheManager {

    private final Map<String, Object> objectMap = new HashMap<>();

    public Object get(String key) {
        return objectMap.getOrDefault(key, null);
    }

    public void save(String key, Object value) {
        objectMap.put(key, value);
    }

    public boolean containsKey(String key) {
        return objectMap.containsKey(key);
    }

    public Set<String> keySet() {
        return objectMap.keySet();
    }

}
