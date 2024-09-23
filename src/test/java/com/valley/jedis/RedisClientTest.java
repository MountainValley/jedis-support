package com.valley.jedis;

import com.valley.jedis.client.factory.ClusterRedisFactory;
import com.valley.jedis.client.factory.RedisFactory;
import com.valley.jedis.client.factory.SentinelRedisFactory;
import com.valley.jedis.client.factory.SimpleRedisFactory;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.UnifiedJedis;


public class RedisClientTest {

    @Test
    public void simpleJedisPoolFactoryUseDefaultFileNameTest() {
        RedisFactory redisFactory = new SimpleRedisFactory();
        UnifiedJedis jedis = redisFactory.getUnifiedJedis();
        String resp = jedis.set("where are you from", "I am from SimpleRedisFactory");
        Assert.assertEquals("OK", resp);
        Assert.assertTrue(jedis.del("where are you from") > 0L);
    }

    @Test
    public void simpleJedisPoolFactoryTest() {
        RedisFactory redisFactory = new SimpleRedisFactory("jedis.properties");
        UnifiedJedis jedis = redisFactory.getUnifiedJedis();
        String resp = jedis.set("where are you from", "I am from SimpleRedisFactory");
        Assert.assertEquals("OK", resp);
        Assert.assertTrue(jedis.del("where are you from") > 0L);
    }

    @Test
    public void sentinelConfigJedisPoolFactoryUseDefaultFileNameTest() {
        RedisFactory redisFactory = new SentinelRedisFactory();
        UnifiedJedis jedis = redisFactory.getUnifiedJedis();
        String resp = jedis.set("where are you from", "I am from SentinelRedisFactory");
        Assert.assertEquals("OK", resp);
        Assert.assertTrue(jedis.del("where are you from") > 0L);
    }

    @Test
    public void sentinelConfigJedisPoolFactoryTest() {
        RedisFactory redisFactory = new SentinelRedisFactory("sentinelJedis.properties");
        UnifiedJedis jedis = redisFactory.getUnifiedJedis();
        String resp = jedis.set("where are you from", "I am from SentinelRedisFactory");
        Assert.assertEquals("OK", resp);
        Assert.assertTrue(jedis.del("where are you from") > 0L);
    }

    @Test
    public void jedisClusterUseDefaultFileNameTest() {
        RedisFactory redisFactory = new ClusterRedisFactory();
        UnifiedJedis jedis = redisFactory.getUnifiedJedis();
        String resp = jedis.set("where are you from", "I am from ClusterRedisFactory");
        Assert.assertEquals("OK", resp);
        Assert.assertTrue(jedis.del("where are you from") > 0L);
    }

    @Test
    public void jedisClusterTest() {
        RedisFactory redisFactory = new ClusterRedisFactory("clusterJedis.properties");
        UnifiedJedis jedis = redisFactory.getUnifiedJedis();
        String resp = jedis.set("where are you from", "I am from ClusterRedisFactory");
        Assert.assertEquals("OK", resp);
        Assert.assertTrue(jedis.del("where are you from") > 0L);
    }
}
