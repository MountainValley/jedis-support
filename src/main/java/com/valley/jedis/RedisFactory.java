package com.valley.jedis;

import redis.clients.jedis.UnifiedJedis;

/**
 * @author penghuanhu
 * @since 2024/9/14
 **/
public interface RedisFactory {
    UnifiedJedis getUnifiedJedis();
}
