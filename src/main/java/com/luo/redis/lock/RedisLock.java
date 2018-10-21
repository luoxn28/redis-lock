package com.luo.redis.lock;

import com.luo.redis.lock.util.SpringUtils;
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
import java.util.stream.Collectors;

/**
 * Created by xiangnan on 2018/10/21.
 */
@Slf4j
public class RedisLock {

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

    private final int ONE_SECOND = 1;
    private int lockLeaseTime = 3 * ONE_SECOND;

    @Getter
    private String lockKey;
    private final String lockUUID;
    private StringRedisTemplate stringRedisTemplate = SpringUtils.getBean(StringRedisTemplate.class);

    public RedisLock(String lockKey) {
        this.lockKey = lockKey;
        this.lockUUID = UUID.randomUUID().toString();
    }

    public Boolean tryLock() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaLock, Boolean.class),
                Arrays.asList(lockKey), lockUUID, String.valueOf(lockLeaseTime));
    }

    public Boolean unlock() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaUnlock, Boolean.class),
                Arrays.asList(lockKey), lockUUID);
    }
}
