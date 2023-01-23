package com.cache.factory;

import com.cache.enums.CacheEnum;
import com.cache.biz.CacheI;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// 单例工厂
public class CacheFactory {
    public static final Map<CacheEnum, String> map = new HashMap<CacheEnum, String>() {{
        put(CacheEnum.LFU, "com.cache.biz.LFUCache");
        put(CacheEnum.LRU, "com.cache.biz.LRUCache");
    }};
    private CacheFactory() {

    }

    public static CacheI getCache(CacheEnum cacheEnum) {
        CacheEnum optionEnum = Optional.ofNullable(cacheEnum).orElse(CacheEnum.LRU);
        String className = Optional.ofNullable(map.get(optionEnum)).orElse("com.cache.biz.LRUCache");

        try {
            Class<?> clazz = Class.forName(className);
            return  (CacheI) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CacheI getCache(CacheEnum cacheEnum, Integer capacity) {
        CacheEnum optionEnum = Optional.ofNullable(cacheEnum).orElse(CacheEnum.LRU);
        String className = Optional.ofNullable(map.get(optionEnum)).orElse("com.cache.biz.LRUCache");
        try {
            Class<?> clazz = Class.forName(className);
            Constructor constructor = clazz.getConstructor(Integer.class);
            return  (CacheI) constructor.newInstance(capacity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        CacheI lruCache = CacheFactory.getCache(CacheEnum.LRU, 5);
//        LRUCache lruCache = new LRUCache(5);
        for (int i = 0; i < 5; i++) {
            lruCache.put("lru"+i, "张三"+i);
        }
        lruCache.put("lru5","李四",1L, TimeUnit.SECONDS);

        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(lruCache.get("lru"+i));
        }
    }

}
