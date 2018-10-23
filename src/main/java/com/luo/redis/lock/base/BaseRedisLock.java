package com.luo.redis.lock.base;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by xiangnan on 2018/10/22.
 */
@Slf4j
@EqualsAndHashCode
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

    /**
     * 是否进行锁续约
     */
    protected volatile boolean renewal = false;

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

    public abstract Boolean renewal();

    /**
     * 锁续约线程池
     */
    private ScheduledThreadPoolExecutor scheduleEexecutor = new ScheduledThreadPoolExecutor(2, new ThreadFactory() {
        private AtomicInteger num = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, BaseRedisLock.this.getClass().getSimpleName() + "-renewal-thread-" + num.getAndIncrement());
        }
    });

    /**
     * 待续约锁集合
     */
    private ConcurrentHashMap<BaseRedisLock, Integer> lockHashMap = new ConcurrentHashMap<>();

    protected void afterLock() {
        BaseRedisLock lock = this;

        if (renewal && lock.lockLeaseTime > 0) {
            synchronized (lock) {
                lockHashMap.put(lock, 0);
                schedule(lock);
            }
        }
    }

    protected synchronized void beforeUnlock() {
        lockHashMap.remove(this);
    }

    private void schedule(BaseRedisLock lock) {
        scheduleEexecutor.schedule(() -> {
            if (!lockHashMap.containsKey(lock)) {
                // 已unlock删除
                return;
            }

            if (!lock.renewal()) {
                int errNum = lockHashMap.get(lock);
                if (++errNum >= 3) {
                    log.error("renewal error, lock:{}, errNum:{}", lock, errNum);
                }
                lockHashMap.put(lock, errNum);
            }

            schedule(lock);
        }, lock.lockLeaseTime * 1000 / 3, TimeUnit.MILLISECONDS);
    }

}
