package com.valley.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.providers.PooledConnectionProvider;

import java.io.IOException;
import java.util.Properties;

/**
 * @author penghuanhu
 * @since 2024/9/14
 **/
public class SimpleRedisFactory implements RedisFactory {
    Logger logger = LoggerFactory.getLogger(getClass());

    private String configName = "jedis.properties";
    private volatile UnifiedJedis unifiedJedis;

    public SimpleRedisFactory() {
    }

    public SimpleRedisFactory(String configName) {
        this.configName = configName;
    }

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
        String url = properties.getProperty("redis.url");
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

        PooledConnectionProvider pooledConnectionProvider = new PooledConnectionProvider(HostAndPort.from(url),jedisClientConfig,poolConfig);
        this.unifiedJedis = new UnifiedJedis(pooledConnectionProvider);
        logger.info(poolConfig.toString());
    }

}
