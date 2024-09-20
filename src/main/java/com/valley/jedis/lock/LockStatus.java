package com.valley.jedis.lock;

import java.util.Objects;

class LockStatus {
    private final String lockKey;
    private final String lockValue;
    private final boolean expiredTimeRenewable;
    private long expireAt;

    public LockStatus(long expireAt, String lockKey, String lockValue, boolean expiredTimeRenewable) {
        this.expireAt = expireAt;
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.expiredTimeRenewable = expiredTimeRenewable;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LockStatus)) {
            return false;
        }
        LockStatus that = (LockStatus) o;
        return Objects.equals(lockKey, that.lockKey) && Objects.equals(lockValue, that.lockValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lockKey, lockValue);
    }

    @Override
    public String toString() {
        return "LockStatus{" +
                "lockKey='" + lockKey + '\'' +
                ", lockValue='" + lockValue + '\'' +
                ", expiredTimeRenewable=" + expiredTimeRenewable +
                ", expireAt=" + expireAt +
                '}';
    }
}
