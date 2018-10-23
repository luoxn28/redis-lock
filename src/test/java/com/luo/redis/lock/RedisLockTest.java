package com.luo.redis.lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Created by xiangnan on 2018/10/21.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication
public class RedisLockTest {

    @Test
    public void test() throws InterruptedException {
        RedisLock lock = new RedisLock(UUID.randomUUID().toString(), false);

        Assert.isTrue(lock.tryLock());
        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(lock.unlock());

        Assert.isTrue(lock.tryLock());
        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(lock.unlock());

        lock = new RedisLock(UUID.randomUUID().toString(), true);
        lock.setLockLeaseTime(1);
        lock.setConnectTimeout(1000);
        Assert.isTrue(lock.tryLock());

        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(lock.unlock());
    }

}
