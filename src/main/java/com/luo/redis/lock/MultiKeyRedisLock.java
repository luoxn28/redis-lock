package com.luo.redis.lock;

import com.luo.redis.lock.base.BaseRedisLock;
import com.luo.redis.lock.util.SpringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

/**
 * Created by xiangnan on 2018/10/22.
 */
@Slf4j
@EqualsAndHashCode
public class MultiKeyRedisLock extends BaseRedisLock {
    public final static String LOCK_OK = "ok";

    private static String luaLock;
    private static String luaUnlock;
    private static String luaRenewal;

    static {
        // load lock/unlock lua script
        try {
            luaLock = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/keys_lock.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            luaUnlock = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/keys_unlock.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            luaRenewal = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/keys_renewal.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            log.error("load lua script error", e);
        } finally {
            Assert.notNull(luaLock);
            Assert.notNull(luaUnlock);
            Assert.notNull(luaRenewal);
        }
    }

    @Getter
    private List<String> lockKeys;
    private final String lockUUID;
    private StringRedisTemplate stringRedisTemplate = SpringUtils.getBean(StringRedisTemplate.class);

    public MultiKeyRedisLock(List<String> lockKeys, boolean renewal) {
        this.lockKeys = lockKeys;
        this.lockUUID = UUID.randomUUID().toString();
    }

    @Override
    public Boolean tryLock() {
        return LOCK_OK.equals(tryLockInternal());
    }

    /**
     * 尝试获得锁，成功返回LOCK_OK，否则返回导致加锁失败对应的key
     */
    public String tryLockWithResult() {
        return tryLockInternal();
    }

    /**
     * 带超时时间尝试获得锁，成功返回LOCK_OK，否则返回导致加锁失败对应的key
     */
    public String tryLockWithResult(int connectTimeout, TimeUnit timeUnit) {
        String result = null;
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(connectTimeout);
        do {
            result = tryLockInternal();
            if (LOCK_OK.equals(tryLockInternal())) {
                break;
            }

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(lockSleepTime));
        } while (System.currentTimeMillis() < endTime);

        return result;
    }

    private String tryLockInternal() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaLock, String.class),
                lockKeys, lockUUID, String.valueOf(lockLeaseTime));
    }

    @Override
    public Boolean unlock() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaUnlock, Boolean.class),
                lockKeys, lockUUID);
    }

    @Override
    public Boolean renewal() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaRenewal, Boolean.class),
                lockKeys, lockUUID, String.valueOf(lockLeaseTime));
    }

}
