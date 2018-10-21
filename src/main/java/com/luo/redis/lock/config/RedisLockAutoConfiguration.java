package com.luo.redis.lock.config;

import com.luo.redis.lock.util.SpringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by xiangnan on 2018/10/21.
 */
@Configuration
public class RedisLockAutoConfiguration {

    @Bean("redis-lock-spring-utils")
    public SpringUtils springUtils() {
        return new SpringUtils();
    }

}
