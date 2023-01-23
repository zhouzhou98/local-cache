package com.cache.template;

import java.util.concurrent.TimeUnit;

public interface LocalCacheTemplate<K, V> {
    // 获取缓存
    Object get(K key);

    // 缓存存储, 设置过期时间
    void put(K key, V value, Long expire, TimeUnit timeUnit);

    void put(K key, V value);

    void remove(K key);
}
