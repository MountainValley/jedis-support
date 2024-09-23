package com.valley.jedis.client.factory;

import com.valley.jedis.util.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 集群模式
 */
public class ClusterRedisFactory implements RedisFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClusterRedisFactory.class);
    private static final ConcurrentHashMap<String, UnifiedJedis> JEDIS_CACHE = new ConcurrentHashMap<>();
    private String configFileName = "clusterJedis.properties";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (JEDIS_CACHE != null && !JEDIS_CACHE.isEmpty()) {
                JEDIS_CACHE.values().forEach(UnifiedJedis::close);
            }
        }));
    }

    public ClusterRedisFactory() {
        initUnifiedJedis();
    }

    public ClusterRedisFactory(String configFileName) {
        this.configFileName = configFileName;
        initUnifiedJedis();
    }

    private void initUnifiedJedis() {
        final String configName_ = this.configFileName;
        if (!JEDIS_CACHE.contains(configName_)) {
            synchronized (SimpleRedisFactory.class) {
                if (!JEDIS_CACHE.contains(configName_)) {
                    JEDIS_CACHE.put(configName_, buildFromConfig(configName_));
                }
            }
        }
    }

    @Override
    public UnifiedJedis getUnifiedJedis() {
        return JEDIS_CACHE.get(this.configFileName);
    }

    private UnifiedJedis buildFromConfig(String configFileName) {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(configFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String clusterNodes = properties.getProperty("cluster.nodes");
        String password = properties.getProperty("redis.password");
        String testOnBorrow = properties.getProperty("pool.testOnBorrow");
        String maxTotal = properties.getProperty("pool.maxTotal");
        String maxIdle = properties.getProperty("pool.maxIdle");

        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .password(password)
                .build();
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(maxTotal));
        poolConfig.setMaxIdle(Integer.parseInt(maxIdle));
        poolConfig.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));
        logger.info(poolConfig.toString());
        return new JedisCluster(StringUtils.parseHostAndPorts(clusterNodes), jedisClientConfig, poolConfig);
    }


}
