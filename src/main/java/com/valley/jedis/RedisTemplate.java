package com.valley.jedis;

import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SetParams;

/**
 * @author penghuanhu
 * @since 2024/9/14
 **/
public class RedisTemplate {
    private final RedisFactory redisFactory;
    public static final String OK = "OK";

    public RedisTemplate(RedisFactory redisFactory) {
        this.redisFactory = redisFactory;
    }

    public String set(String key, String value) {
        return getUnifiedJedis().set(key, value);
    }

    public String set(String key, String value, SetParams setParams) {
        return getUnifiedJedis().set(key, value, setParams);
    }

    public String get(String key) {
        return getUnifiedJedis().get(key);
    }

    public long del(String key){
        return getUnifiedJedis().del(key);
    }

    public UnifiedJedis getUnifiedJedis() {
        return redisFactory.getUnifiedJedis();
    }
}
