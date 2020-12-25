package com.matt.project.seckill.service;

/**
 * 封装本地缓存
 * @author matt
 * @create 2020-12-20 14:27
 */
public interface CacheService {

    /**
     * 功能：存取
     * @author matt
     * @date 2020/12/20
     * @param key
     * @param value
     * @return void
    */
    void setCommonCache(String key, Object value);

    /**
     * 功能：获取
     * @author matt
     * @date 2020/12/20
     * @param key
     * @return java.lang.Object
    */
    Object getFromCommonCache(String key);

    void deleteCommonCacheByKey(String key);


}
