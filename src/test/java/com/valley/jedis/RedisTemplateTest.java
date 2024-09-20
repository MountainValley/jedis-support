package com.valley.jedis;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author penghuanhu
 * @since 2024/9/14
 **/
public class RedisTemplateTest {
    @Test
    public void simpleJedisPoolFactoryTest(){
        RedisFactory redisFactory = new SimpleRedisFactory();
        RedisTemplate redisTemplate = new RedisTemplate(redisFactory);
        String resp = redisTemplate.set("where are you from","I am from SimpleRedisFactory");
        Assert.assertEquals("OK", resp);
    }

    @Test
    public void sentinelConfigJedisPoolFactoryTest(){
        RedisFactory redisFactory = new SentinelRedisFactory();
        RedisTemplate redisTemplate = new RedisTemplate(redisFactory);
        String resp = redisTemplate.set("where are you from","I am from SentinelRedisFactory");
        Assert.assertEquals("OK", resp);
    }

    @Test
    public void jedisClusterTest(){
        RedisFactory redisFactory = new ClusterRedisFactory();
        RedisTemplate redisTemplate = new RedisTemplate(redisFactory);
        String resp = redisTemplate.set("where are you from","I am from ClusterRedisFactory");
        Assert.assertEquals("OK", resp);
    }}
