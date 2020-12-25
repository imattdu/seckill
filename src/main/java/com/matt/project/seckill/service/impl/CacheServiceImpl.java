package com.matt.project.seckill.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.matt.project.seckill.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author matt
 * @create 2020-12-20 14:31
 */
@Service
public class CacheServiceImpl implements CacheService {


    private Cache<String, Object> commomCache = null;

    /**
     * 功能： bean初始化会被执行
     * @author matt
     * @date 2020/12/20
     * @param
     * @return void
    */
    @PostConstruct
    public void init() {
        commomCache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();


    }

    @Override
    public void setCommonCache(String key, Object value) {
        commomCache.put(key, value);
    }

    @Override
    public Object getFromCommonCache(String key) {


        // commomCache 如果存在否则返回null
        return commomCache.getIfPresent(key);
    }

    @Override
    public void deleteCommonCacheByKey(String key) {
        commomCache.invalidate(key);
    }
}
