package com.valley.jedis.lock;

class LockStatus {
    private final String lockKey;
    private final String lockValue;
    private final boolean expiredTimeRenewable;
    private final Thread owner;
    private long expireAt;

    public LockStatus(long expireAt, String lockKey, String lockValue, boolean expiredTimeRenewable, Thread owner) {
        this.expireAt = expireAt;
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.expiredTimeRenewable = expiredTimeRenewable;
        this.owner = owner;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public String getLockKey() {
        return lockKey;
    }

    public String getLockValue() {
        return lockValue;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    public boolean isExpiredTimeRenewable() {
        return expiredTimeRenewable;
    }

    public Thread getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "LockStatus{" +
                "lockKey='" + lockKey + '\'' +
                ", lockValue='" + lockValue + '\'' +
                ", expiredTimeRenewable=" + expiredTimeRenewable +
                ", owner=" + owner +
                ", expireAt=" + expireAt +
                '}';
    }
}
