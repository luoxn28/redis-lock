package com.luo.redis.lock;

import com.luo.redis.lock.base.BaseRedisLock;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by xiangnan on 2018/10/22.
 */
@Slf4j
public class MultiKeyRedisLock extends BaseRedisLock {
    public final static String LOCK_OK = "ok";

    private static String luaLock;
    private static String luaUnlock;

    static {
        // load lock/unlock lua script
        try {
            luaLock = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/keys_lock.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            luaUnlock = new BufferedReader(new InputStreamReader(new ClassPathResource("lua/keys_unlock.lua").getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            log.error("load lua script error", e);
        } finally {
            Assert.notNull(luaLock);
            Assert.notNull(luaUnlock);
        }
    }

    @Getter
    private List<String> lockKeys;
    private final String lockUUID;
    private StringRedisTemplate stringRedisTemplate = SpringUtils.getBean(StringRedisTemplate.class);

    public MultiKeyRedisLock(List<String> lockKeys) {
        this.lockKeys = lockKeys;
        this.lockUUID = UUID.randomUUID().toString();
    }

    @Override
    public Boolean tryLock() {
        String result = stringRedisTemplate.execute(new DefaultRedisScript<>(luaLock, String.class),
                lockKeys, lockUUID, String.valueOf(lockLeaseTime));
        return Objects.nonNull(result) && LOCK_OK.equals(result);
    }

    @Override
    public Boolean unlock() {
        return stringRedisTemplate.execute(new DefaultRedisScript<>(luaUnlock, Boolean.class),
                lockKeys, lockUUID);
    }
}
