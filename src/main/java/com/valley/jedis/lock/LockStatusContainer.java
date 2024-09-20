package com.valley.jedis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

class LockStatusContainer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HashMap<String, LockStatus> lockMap = new HashMap<>();
    private final TreeSet<LockStatus> lockSet = new TreeSet<>(Comparator.comparing(LockStatus::getExpireAt));

    public synchronized void add(LockStatus status) {
        LockStatus lockStatus = lockMap.get(status.getLockKey());
        if (lockStatus != null) {
            lockSet.remove(lockStatus);
        }

        lockMap.put(status.getLockKey(), status);
        lockSet.add(status);
        logger.debug("LockStatusContainer add item: {}", status);
    }

    public synchronized void remove(String lockKey, String lockValue) {
        LockStatus lockStatus = lockMap.get(lockKey);
        if (lockStatus != null && Objects.equals(lockStatus.getLockValue(), lockValue)) {
            lockMap.remove(lockKey);
            lockSet.remove(lockStatus);
            logger.debug("LockStatusContainer remove item: {}", lockStatus);
        }
    }

    public LockStatus getLockStatus(String lockKey) {
        return lockMap.get(lockKey);
    }

    public TreeSet<LockStatus> getLockStatus() {
        return new TreeSet<>(lockSet);
    }
}
