package com.luo.redis.lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by xiangnan on 2018/10/22.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiKeyRedisLockTest {

    @Test
    public void simpleTest() {
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();
        List<String> lockKeys = Arrays.asList(key1, key2);
        MultiKeyRedisLock lock = new MultiKeyRedisLock(lockKeys);

        Assert.isTrue(lock.tryLock());
        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(lock.unlock());
    }
}
