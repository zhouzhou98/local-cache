package com.cache.example;

import com.cache.template.LocalCacheTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheTest {
    @Autowired
    private LocalCacheTemplate<Object, Object> localCacheTemplate;
    @RequestMapping("test")
    public String text() {
        localCacheTemplate.put("1", "2");
        return localCacheTemplate.get("1").toString();
    }
}
