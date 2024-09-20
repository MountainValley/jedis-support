package com.valley.jedis;

import com.valley.jedis.lock.RedisLock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class RedisLockTest {
    Logger logger = LoggerFactory.getLogger(getClass());
    private static RedisLock redisLock;
    private static RedisTemplate redisTemplate;
    private static final String LOCK_KEY = "testLock";

    @BeforeClass
    public static void beforeClass() {
        RedisFactory redisFactory = new SimpleRedisFactory();
        redisTemplate = new RedisTemplate(redisFactory);
        redisLock = new RedisLock(redisTemplate);
    }

    @Before
    public void before(){
        redisTemplate.del(LOCK_KEY);
    }

    @Test
    public void testLock() {
        String lockValue = redisLock.tryLock(LOCK_KEY, 2);
        Assert.assertNotNull(lockValue);

        long endTime = System.currentTimeMillis() + 30 * 1000L;
        new Thread(() -> {
            while (System.currentTimeMillis() <= endTime) {
                Assert.assertNull(redisLock.tryLock(LOCK_KEY, 2));
                Assert.assertFalse(redisLock.releaseLock(LOCK_KEY,lockValue));
            }
        }).start();

        Assert.assertTrue(redisLock.releaseLock(LOCK_KEY,lockValue));
    }

    @Test
    public void testLockWithReleaseSeconds() {
        String lockValue = redisLock.tryLock(LOCK_KEY, 2, 5);
        Assert.assertNotNull(lockValue);
        Assert.assertFalse(redisLock.releaseLock(LOCK_KEY, lockValue+"_"));
        Assert.assertTrue(redisLock.releaseLock(LOCK_KEY,lockValue));
    }


    @Test
    public void tryLockConcurrent() {
        final CountDownLatch countDownLatch = new CountDownLatch(3);

        Thread thread1 = new Thread(() -> {
            String lockValue = "";
            try {
                lockValue = redisLock.tryLock(LOCK_KEY, 2);
                logger.debug("Thread:{} acquired a lock key:{} value:{}", Thread.currentThread(), LOCK_KEY, lockValue);
                Assert.assertNotNull(lockValue);
                try {
                    Thread.sleep(20 * 1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                Assert.assertTrue(redisLock.releaseLock(LOCK_KEY, lockValue));
                countDownLatch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                String lockValue = redisLock.tryLock(LOCK_KEY, 18);
                logger.debug("Thread:{} acquired a lock key:{} value:{}", Thread.currentThread(), LOCK_KEY, lockValue);
                Assert.assertNull(lockValue);
            } finally {
                countDownLatch.countDown();
            }
        });

        Thread thread3 = new Thread(() -> {
            String lockValue = "";
            try {
                lockValue = redisLock.tryLock(LOCK_KEY, 21);
                logger.debug("Thread:{} acquired a lock key:{} value:{}", Thread.currentThread(), LOCK_KEY, lockValue);
                Assert.assertNotNull(lockValue);
            } finally {
                Assert.assertTrue(redisLock.releaseLock(LOCK_KEY, lockValue));
                countDownLatch.countDown();
            }
        });

        thread1.start();
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread2.start();
        thread3.start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
