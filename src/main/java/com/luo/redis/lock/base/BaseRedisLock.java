package com.luo.redis.lock.base;

import lombok.Setter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by xiangnan on 2018/10/22.
 */
public abstract class BaseRedisLock {

    protected final int ONE_SECOND = 1;

    /**
     * 默认加锁时间3s
     */
    @Setter
    protected int lockLeaseTime = 3 * ONE_SECOND;

    /**
     * 加锁失败默认休息1ms
     */
    protected long lockSleepTime = 1;

    public abstract Boolean tryLock();

    public Boolean tryLock(int connectTimeout, TimeUnit timeUnit) {
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(connectTimeout);
        do {
            if (tryLock()) {
                return true;
            }

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(lockSleepTime));
        } while (System.currentTimeMillis() < endTime);

        return false;
    }

    public abstract Boolean unlock();

}
