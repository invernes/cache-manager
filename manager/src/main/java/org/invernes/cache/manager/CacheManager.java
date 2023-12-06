package org.invernes.cache.manager;

import java.util.Set;

// todo add java doc, refactor methods
public interface CacheManager {

    Object get(String key);

    void save(String key, Object value);

    boolean containsKey(String key);

    Set<String> keySet();
}
