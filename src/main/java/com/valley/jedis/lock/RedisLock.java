package com.valley.jedis.lock;

import com.valley.jedis.client.factory.RedisFactory;
import com.valley.jedis.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SetParams;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于jedis实现的分布式锁
 * <P>支持功能&实现原理：</P>
 * <ul>
 *     <li>可重入锁：通过ThreadLocal变量记录当前线程持有锁的数量，当锁数量再次归零时删除对应redis key</li>
 *     <li>自动续期：加锁成功后定期检查锁状态，对即将过期但线程仍处于存活状态的锁进行自动续期</li>
 *     <li>自动失效：通过redis自动过期机制实现锁超期自动释放</li>
 * </ul>
 *
 * <P>暂时不支持功能&注意事项</P>
 * <ul>
 *     <li>需要自动续期和重入锁功能时请不要手动设置锁过期时间（设置为-1L视为未设置过期时间）</li>
 *     <li>暂时不支持死锁识别。获取锁需要提供最长等待时间参数(waitSeconds)以避免长时间死锁导致线程被永久阻塞。（TODO:可以考虑在redis上记录当前线程已持有的锁和正在申请中的锁信息以实现死锁识别。）</li>
 * </ul>
 *
 **/
public class RedisLock {
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);

    /**
     * 记录当前线程锁状态
     */
    private final ThreadLocal<Map<String, ThreadLockStatus>> THREAD_LOCK = ThreadLocal.withInitial(HashMap::new);

    /**
     * 记录所有线程已获取的锁状态
     */
    private final LockStatusContainer LOCK_STATUS_CONTAINER = new LockStatusContainer();

    /**
     * 定时续活线程池
     */
    private final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "RedisLock-WatchDog"));


    private final UnifiedJedis unifiedJedis;

    public RedisLock(RedisFactory redisFactory) {
        this.unifiedJedis = redisFactory.getUnifiedJedis();

        SCHEDULED_THREAD_POOL_EXECUTOR.setRemoveOnCancelPolicy(true);
        long schedulePeriod = LockConfig.DEFAULT_REDIS_KEY_EXPIRE_MILLIS / 3;
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(this::tryProlong, schedulePeriod, schedulePeriod, TimeUnit.MILLISECONDS);
    }

    /**
     * 锁申请
     * <p>通过此方法获取的锁将自动续期，仅当执行手动解锁或取得锁的线程崩溃或长时间不可用才会最终释放锁。</p>
     * <p>通过此方法获取的锁支持锁重入</p>
     *
     * @param lockKey     redis lock key
     * @param waitSeconds 获取锁动作超时时间
     * @return 获取锁成功时返回锁标识，失败时返回null
     */
    public String tryLock(String lockKey, long waitSeconds) {
        return this.tryLock(lockKey, waitSeconds, -1L);
    }

    /**
     * 锁申请
     * <p>注意：通过此方法获取锁的最长持有时间受到releaseSeconds参数限制。即不会被自动续期且不支持锁重入功能。</p>
     *
     * @param lockKey        redis lock key
     * @param waitSeconds    获取锁动作超时时间
     * @param releaseSeconds redis锁自动释放时间
     * @return 获取锁成功时返回锁标识，失败时返回null
     */
    public String tryLock(String lockKey, long waitSeconds, long releaseSeconds) {
        String lockValue;

        //锁重入
        ThreadLockStatus threadLockStatus = THREAD_LOCK.get().get(lockKey);
        if (threadLockStatus != null) {
            LockStatus lockStatus = LOCK_STATUS_CONTAINER.getLockStatus(lockKey);
            if (lockStatus != null && Objects.equals(lockStatus.getLockValue(), threadLockStatus.getLockValue())) {
                if (!lockStatus.isExpiredTimeRenewable()) {
                    throw new UnsupportedOperationException("The lock with an expiration time does not support reentrancy.");
                }
                prolongLock(lockStatus);
            } else {
                //线程变量中记录的锁信息和全局变量不一致意味着线程所持有锁已经过期or丢失
                logger.warn("find lock expired when reentry. lockKey:{}", lockKey);
            }

            //只要线程变量中记录有锁信息无论锁信息在redis中是否已失效均当做未失效情况允许锁重入。
            long lockedTimes = threadLockStatus.incrLockCount();
            logger.debug("reentry lock:{} {} times by thread:{}", lockKey, lockedTimes, Thread.currentThread());
            return threadLockStatus.getLockValue();
        }

        long tryAcquireBefore = System.currentTimeMillis() + waitSeconds * 1000L;
        lockValue = UUID.randomUUID().toString().replaceAll("-", "");
        long sleepTime = 50L;
        while (System.currentTimeMillis() <= tryAcquireBefore) {
            boolean acquired = tryLockOnce(lockKey, lockValue, releaseSeconds);
            if (acquired) {
                return lockValue;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("tryLock interrupted", e);
                return null;
            }
            sleepTime = Math.min(sleepTime * 2, 500L) + new Random().nextInt(50);
        }
        return null;
    }

    private boolean tryLockOnce(String lockKey, String lockValue, long releaseSeconds) {
        boolean acquired = false;
        boolean expiredTimeRenewable = (releaseSeconds == -1L);
        long expireAtMilliseconds;
        if (expiredTimeRenewable) {
            expireAtMilliseconds = System.currentTimeMillis() + LockConfig.DEFAULT_REDIS_KEY_EXPIRE_MILLIS;
        } else {
            expireAtMilliseconds = System.currentTimeMillis() + releaseSeconds * 1000L;
        }
        try {
            String resp = unifiedJedis.set(lockKey, lockValue, new SetParams().nx().pxAt(expireAtMilliseconds));
            logger.debug("redis set lock key:{} value:{} expireAt:{} resp:{}", lockKey, lockValue, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expireAtMilliseconds), resp);
            acquired = "OK".equals(resp);

            if (acquired) {
                ThreadLockStatus threadLockStatus = new ThreadLockStatus(lockValue);
                THREAD_LOCK.get().put(lockKey, threadLockStatus);
                logger.debug("put threadLocal key:{} threadLockStatus:{}", lockKey, threadLockStatus);
                LockStatus lockStatus = new LockStatus(expireAtMilliseconds, lockKey, lockValue, expiredTimeRenewable);
                LOCK_STATUS_CONTAINER.add(lockStatus);
            }
        } catch (Exception e) {
            logger.warn("tryLockOnce failed. lockKey:{}, lockValue:{}, releaseSeconds:{}", lockKey, lockValue, releaseSeconds, e);
        }
        return acquired;
    }

    /**
     * 锁释放
     *
     * @param lockKey   redis lock key
     * @param lockValue redis lock value
     * @return 锁释放结果
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        return this.releaseLock(lockKey,lockValue,true);
    }

    /**
     * 锁释放
     *
     * @param lockKey   redis lock key
     * @param lockValue redis lock value
     * @param deleteByOwner 是否是线程释放自己的锁
     * @return 锁释放结果
     */
    private boolean releaseLock(String lockKey, String lockValue, boolean deleteByOwner) {
        if (StringUtils.isAnyBlank(lockKey, lockValue)) {
            throw new IllegalArgumentException("blank argument founded. lockKey:" + lockKey + "lockValue:" + lockValue);
        }
        //重入锁处理
        if (deleteByOwner){
            ThreadLockStatus threadLockStatus = THREAD_LOCK.get().get(lockKey);
            if (threadLockStatus == null) {
                logger.warn("release lock failed. the lock can only be released by it's owner. lockKey:{},lockValue:{}", lockKey, lockValue);
                return false;
            } else if (!Objects.equals(lockValue, threadLockStatus.getLockValue())) {
                logger.warn("releaseLock failed. invalid lockValue. key:{} inputValue:{} acquiredValue:{}", lockKey, lockValue, threadLockStatus.getLockValue());
                return false;
            }
            if (threadLockStatus.decrLockCount() > 0) {
                return true;
            }

            THREAD_LOCK.get().remove(lockKey);
            logger.debug("remove threadLocal key:{} threadLockStatus:{}", lockKey, threadLockStatus);
        }

        LOCK_STATUS_CONTAINER.remove(lockKey, lockValue);
        Object object = unifiedJedis.eval(LockConfig.LUA_SCRIPT_RELEASE_LOCK, Collections.singletonList(lockKey), Collections.singletonList(lockValue));
        if (object instanceof Long && (Long) object == 1L) {
            logger.debug("releaseLock key:{} value:{}", lockKey, lockValue);
            return true;
        }
        logger.warn("releaseLock failed. key:{} value:{}", lockKey, lockValue);
        return false;
    }


    /**
     * 锁续活
     *
     * @param lockStatus lockStatus
     */
    private void prolongLock(LockStatus lockStatus) {
        String lockKey = lockStatus.getLockKey();
        String lockValue = lockStatus.getLockValue();
        long newExpireAt = System.currentTimeMillis() + (LockConfig.DEFAULT_REDIS_KEY_EXPIRE_MILLIS * 4 / 3);
        Object object = unifiedJedis.eval(LockConfig.LUA_SCRIPT_PROLONG_LOCK, Collections.singletonList(lockKey), Arrays.asList(lockValue, newExpireAt + ""));
        if (object instanceof Long && (Long) object == 1L) {
            lockStatus.setExpireAt(newExpireAt);
            logger.debug("prolongLock success. lockKey:{} lockValue:{} reset expireAt:{}", lockKey, lockValue, newExpireAt);
        } else {
            logger.debug("prolongLock failed. lockKey:{} lockValue:{}", lockKey, lockValue);
        }
    }

    /**
     * 检查所有锁并根据情况进行续活
     */
    private void tryProlong() {
        try {
            for (LockStatus lockStatus : LOCK_STATUS_CONTAINER.getLockStatus()) {
                if (!lockStatus.isOwnerThreadAlive() || lockStatus.getExpireAt() < System.currentTimeMillis()){
                    releaseLock(lockStatus.getLockKey(),lockStatus.getLockValue(),false);
                } else if (lockStatus.isExpiredTimeRenewable()) {
                    if (lockStatus.getExpireAt() < System.currentTimeMillis() + LockConfig.DEFAULT_REDIS_KEY_EXPIRE_MILLIS) {
                        prolongLock(lockStatus);
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("scheduled tryProlong task failed.", e);
        }
    }

}
