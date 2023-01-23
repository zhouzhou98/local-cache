package com.cache.biz;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LFUCache<K, V> extends AbstractCache<K, V>{
    private ConcurrentHashMap<Object, Cache> map;

    public LFUCache(Integer capacity) {
        super(capacity);
        this.map = new ConcurrentHashMap<>(capacity);
        new Thread(new TimeoutTimerThread()).start();
    }

    public LFUCache() {
        super(2 << 10);
        this.map = new ConcurrentHashMap<>(2 << 10);
        //  定时删除
        new Thread(new TimeoutTimerThread()).start();
    }
    @Override
    protected Integer getConcurrentHashMapSize() {
        return this.map.size();
    }

    @Override
    protected Object doGet(K key) {
        if (map.isEmpty()) {
            return null;
        }
        if (!map.containsKey(key)) {
            return null;
        }
        Cache cache = map.get(key);
        if (cache == null) {
            return null;
        }
        // 惰性删除
        long timoutTime =cache.getTimeUnit().toSeconds(System.currentTimeMillis()
                - cache.getWriteTime());
        if (cache.getExpireTime() < timoutTime) {
            map.remove(cache.getKey());
            return null;
        }
        cache.setHitCount(cache.getHitCount() + 1);
        cache.setAccessTime(System.currentTimeMillis());
        return cache.getValue();
    }

    @Override
    protected void doRemove(K key) {
        if (map.isEmpty()) {
            return;
        }
        if (!map.containsKey(key)) {
            return;
        }
        Cache cache = map.get(key);
        if (cache == null) {
            return;
        }
        map.remove(cache.getKey());
    }

    /**
     * 获取最少使用的缓存
     * @return
     */
    private Object getKickedKey() {
        Cache min = Collections.min(map.values());
        return min.getKey();
    }
    @Override
    protected void doPut(K key, V value, Long expire, TimeUnit timeUnit) {
        // 当缓存存在时，更新缓存
        if (map.containsKey(key)){
            Cache cache = map.get(key);
            cache.setHitCount(cache.getHitCount() + 1);
            cache.setWriteTime(System.currentTimeMillis());
            cache.setAccessTime(System.currentTimeMillis());
            cache.setTimeUnit(timeUnit != null ? timeUnit : TimeUnit.SECONDS);
            cache.setExpireTime(expire != null ? expire : 7 * 24 * 60 * 60 * 1000 );
            cache.setValue(value);
            return;
        }
        // 已经达到最大缓存
        if (isFull()) {
            Object kickedKey = getKickedKey();
            if (kickedKey !=null){
                // 移除最少使用的缓存
                map.remove(kickedKey);
            }else {
                return;
            }
        }
        Cache cache = new Cache();
        cache.setKey(key);
        cache.setValue(value);
        cache.setWriteTime(System.currentTimeMillis());
        cache.setAccessTime(System.currentTimeMillis());
        cache.setHitCount(1);
        cache.setTimeUnit(timeUnit != null ? timeUnit : TimeUnit.SECONDS);
        cache.setExpireTime(expire != null ? expire : 7 * 24 * 60 * 60 * 1000 );
        map.put(key, cache);
    }

    /**
     * 处理过期缓存
     */
    class TimeoutTimerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    expireCache();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 创建多久后，缓存失效
         *
         * @throws Exception
         */
        private void expireCache() throws Exception {
            System.out.println("检测缓存是否过期缓存");
            for (Object key : map.keySet()) {
                Cache cache = map.get(key);
                long timoutTime = cache.getTimeUnit().toSeconds(System.currentTimeMillis()
                        - cache.getWriteTime());
                if (cache.getExpireTime() > timoutTime) {
                    continue;
                }
                System.out.println(" 清除过期缓存 ： " + key);
                //清除过期缓存
                map.remove(key);
            }
        }
    }

    public static void main(String[] args) {
        LFUCache lfuCache = new LFUCache(5);
        for (int i = 0; i < 5; i++) {
            lfuCache.put("lru"+i, "张三"+i);
        }
        lfuCache.put("lru5","李四",1L, TimeUnit.SECONDS);

        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(lfuCache.get("lru"+i));
        }
    }
}
