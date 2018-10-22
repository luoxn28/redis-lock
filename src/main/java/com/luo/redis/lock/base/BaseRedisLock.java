package com.luo.redis.lock.base;

/**
 * Created by xiangnan on 2018/10/22.
 */
public abstract class BaseRedisLock {

    protected final int ONE_SECOND = 1;
    protected int lockLeaseTime = 3 * ONE_SECOND;

    public abstract Boolean tryLock();

    public abstract Boolean unlock();

}
