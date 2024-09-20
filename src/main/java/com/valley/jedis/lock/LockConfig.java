package com.valley.jedis.lock;

public class LockConfig {
    /**
     * 释放锁-lua脚本
     */
     public static final String LUA_SCRIPT_RELEASE_LOCK =
            "if redis.call(\"get\",KEYS[1]) == ARGV[1] then " +
                    "    return redis.call(\"del\",KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";
    /**
     * 锁续活-lua脚本
     */
    public static final String LUA_SCRIPT_PROLONG_LOCK =
            "if redis.call(\"get\",KEYS[1]) == ARGV[1] then " +
                    "    return redis.call(\"PEXPIREAT\",KEYS[1],ARGV[2]) " +
                    "else " +
                    "    return 0 " +
                    "end";


    /**
     * redis中缓存lock key默认过期毫秒数
     */
    public static final long DEFAULT_REDIS_KEY_EXPIRE_MILLIS = 30 * 1000L;


}
