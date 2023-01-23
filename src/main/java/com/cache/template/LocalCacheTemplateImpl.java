package com.cache.template;

import com.cache.biz.CacheI;
import com.cache.enums.CacheEnum;
import com.cache.factory.CacheFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
@Component
public class LocalCacheTemplateImpl<K, V> implements LocalCacheTemplate<K, V>{
    @Value("${local.cache.type:LRU}")
    private String cacheType;
    @Value("${local.cache.capacity:0}")
    private Integer capacity;
    private CacheI cache;
    @Override
    public Object get(K key) {
        if (cache == null) {
            CacheEnum cacheEnum = CacheEnum.getEnumByValue(cacheType);
            if (this.capacity != 0 ) {
                cache = CacheFactory.getCache(cacheEnum, capacity);
            }
            cache = CacheFactory.getCache(cacheEnum);
        }

        return cache.get(key);
    }

    @Override
    public void put(K key, V value, Long expire, TimeUnit timeUnit) {

        if (cache == null) {
            CacheEnum cacheEnum = CacheEnum.getEnumByValue(cacheType);
            if (this.capacity != 0 ) {
                cache = CacheFactory.getCache(cacheEnum, capacity);
            }
            cache = CacheFactory.getCache(cacheEnum);
        }
        cache.put(key, value, expire, timeUnit);
    }

    @Override
    public void put(K key, V value) {


        if (cache == null) {
            CacheEnum cacheEnum = CacheEnum.getEnumByValue(cacheType);
            if (this.capacity != 0 ) {
                cache = CacheFactory.getCache(cacheEnum, capacity);
            }
            cache = CacheFactory.getCache(cacheEnum);
        }
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        if (cache == null) {
            CacheEnum cacheEnum = CacheEnum.getEnumByValue(cacheType);
            if (this.capacity != 0 ) {
                cache = CacheFactory.getCache(cacheEnum, capacity);
            }
            cache = CacheFactory.getCache(cacheEnum);
        }
        cache.remove(key);
    }
}
