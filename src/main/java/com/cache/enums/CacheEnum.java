package com.cache.enums;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CacheEnum {
    LRU("LRU"),
    LFU("LFU");
    @Getter
    @Setter
    private String value;
    CacheEnum(String value) {
        this.value = value;
    }

    public static List<String> getValues() {
        return Arrays.stream(values()).map(CacheEnum::getValue).collect(Collectors.toList());
    }

    public static CacheEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (CacheEnum cacheEnum : CacheEnum.values()) {
            if (cacheEnum.value.equals(value)) {
                return cacheEnum;
            }
        }
        return null;
    }
}
