package com.valley.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author penghuanhu
 * @since 2024/9/14
 **/
public class ClusterRedisFactory implements RedisFactory {
    Logger logger = LoggerFactory.getLogger(getClass());
    private String configName = "clusterJedis.properties";

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
        this.unifiedJedis = new JedisCluster(parseHostAndPorts(clusterNodes),jedisClientConfig,poolConfig);
        logger.info(poolConfig.toString());
    }

    private static Set<HostAndPort> parseHostAndPorts(String sentinels) {
        return Arrays.stream(sentinels.split(",")).map(HostAndPort::from).collect(Collectors.toSet());
    }

    public ClusterRedisFactory() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (unifiedJedis != null) {
                unifiedJedis.close();  // 确保关闭连接池
            }
        }));
    }

    public ClusterRedisFactory(String configName) {
        this();
        this.configName = configName;
    }
}
