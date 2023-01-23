package com.cache.biz;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCache<K, V> implements CacheI<K, V>{
//    protected ConcurrentHashMap<K, Object> map;

    private Integer capacity;

    protected Integer getCapacity() {
        return this.capacity;
    }
    public AbstractCache(Integer capacity) {
        this.capacity = capacity;
//        this.map = new ConcurrentHashMap<>(capacity);
    }

    protected abstract Integer getConcurrentHashMapSize();
    /**
     * 检查数据是否为空值
     */
    private <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * 判断数据值是否满了
     */
    protected boolean isFull() {
        return this.getConcurrentHashMapSize().equals(capacity);
    }

    // 获取缓存值
    protected abstract Object doGet(K key);

    protected abstract void doRemove(K key);
    // 存放缓存值
    protected abstract void doPut(K key, V value, Long expire, TimeUnit timeUnit);
    @Override
    public Object get(K key) {
        checkNotNull(key);
        return doGet(key);
    }

    @Override
    public void put(K key, V value, Long expire, TimeUnit timeUnit) {
        checkNotNull(key);
        checkNotNull(value);
        checkNotNull(expire);
        doPut(key, value, expire, timeUnit);
    }

    @Override
    public void put(K key, V value) {
        checkNotNull(key);
        checkNotNull(value);
        doPut(key, value, null, null);
    }

    @Override
    public void remove(K key) {
        this.doRemove(key);
    }
}
