package com.valley.jedis;

import redis.clients.jedis.UnifiedJedis;


public interface RedisFactory {
    UnifiedJedis getUnifiedJedis();
}
