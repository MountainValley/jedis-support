package com.valley.jedis.lock;

class ThreadLockStatus {
    private long lockCount;
    private String lockValue;

    public ThreadLockStatus(String lockValue) {
        this.lockValue = lockValue;
    }

    public long incrLockCount() {
        return ++lockCount;
    }

    public long decrLockCount() {
        return --lockCount;
    }

    public String getLockValue() {
        return lockValue;
    }

    @Override
    public String toString() {
        return "ThreadLockStatus{" +
                "lockCount=" + lockCount +
                ", lockValue='" + lockValue + '\'' +
                '}';
    }
}
