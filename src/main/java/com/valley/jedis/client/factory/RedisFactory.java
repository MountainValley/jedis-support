package com.valley.jedis.client.factory;

import redis.clients.jedis.UnifiedJedis;


public interface RedisFactory {
    UnifiedJedis getUnifiedJedis();
}
