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
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by xiangnan on 2018/10/21.
 */
@Slf4j
@EqualsAndHashCode
public class RedisLock extends BaseRedisLock {

    private static String luaLock;
    private static String luaUnlock;

    static {
        try {
            luaLock = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/lock.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            luaUnlock = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/unlock.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            log.error("load lua script error", e);
        } finally {
            Assert.notNull(luaLock, "can't found lock lua script");
            Assert.notNull(luaUnlock, "can't found unlock lua script");
        }
    }

    @Getter
    private String lockKey;
    private final String lockUUID;
    private StringRedisTemplate stringRedisTemplate = SpringUtils.getBean(StringRedisTemplate.class);

    public RedisLock(String lockKey, boolean renewal) {
        this.lockKey = lockKey;
        this.lockUUID = UUID.randomUUID().toString();
        this.renewal = renewal;
    }

    @Override
    public Boolean doTryLock() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaLock, Boolean.class),
                Arrays.asList(lockKey), lockUUID, String.valueOf(lockLeaseTime));
    }

    @Override
    public Boolean doUnlock() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaUnlock, Boolean.class),
                Arrays.asList(lockKey), lockUUID);
    }

    @Override
    public Boolean renewal() {
        try {
            return stringRedisTemplate.expire(lockKey, lockLeaseTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("renewal error", e);
            return false;
        }
    }
}
