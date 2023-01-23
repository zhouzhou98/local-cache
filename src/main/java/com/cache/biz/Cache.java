package com.cache.biz;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class Cache  implements Comparable<Cache> {
    // 键
    private Object key;
    // 缓存值
    private Object value;
    // 最后一次访问时间
    private Long accessTime;
    // 创建时间
    private Long writeTime;
    // 存活时间
    private Long expireTime;
    // 命中次数
    private Integer hitCount;

    private TimeUnit timeUnit;

    @Override
    public int compareTo(Cache o) {
        if (hitCount.compareTo(o.hitCount) == 0) {
            return accessTime.compareTo(o.accessTime);
        }
        return hitCount.compareTo(o.hitCount);
    }
}
