package com.valley.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.providers.SentineledConnectionProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;


public class SentinelRedisFactory implements RedisFactory {
    Logger logger = LoggerFactory.getLogger(getClass());
    private String configName = "sentinelJedis.properties";

    private volatile UnifiedJedis unifiedJedis;

    @Override
    public UnifiedJedis getUnifiedJedis() {
        if (unifiedJedis == null) {
            synchronized (this) {
                if (unifiedJedis == null) {
                    buildFromConfig();
                }
            }
        }
        return unifiedJedis;
    }

    private void buildFromConfig() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(this.configName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String sentinels = properties.getProperty("sentinel.urls");
        String master = properties.getProperty("sentinel.masterName");
        String database = properties.getProperty("redis.database");
        String testOnBorrow = properties.getProperty("pool.testOnBorrow");
        String maxTotal = properties.getProperty("pool.maxTotal");
        String maxIdle = properties.getProperty("pool.maxIdle");

        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .database(Integer.parseInt(database))
                .build();
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(maxTotal));
        poolConfig.setMaxIdle(Integer.parseInt(maxIdle));
        poolConfig.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));
        SentineledConnectionProvider sentineledConnectionProvider =
                new SentineledConnectionProvider(master, jedisClientConfig, poolConfig, parseHostAndPorts(sentinels), DefaultJedisClientConfig.builder().build());

        this.unifiedJedis = new UnifiedJedis(sentineledConnectionProvider);
        logger.info(poolConfig.toString());
    }

    private static Set<HostAndPort> parseHostAndPorts(String sentinels) {
        return Arrays.stream(sentinels.split(",")).map(HostAndPort::from).collect(Collectors.toSet());
    }

    public SentinelRedisFactory() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (unifiedJedis != null) {
                unifiedJedis.close();  // 确保关闭连接池
            }
        }));
    }

    public SentinelRedisFactory(String configName) {
        this();
        this.configName = configName;
    }
}
