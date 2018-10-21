package com.luo.redis.lock.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by xiangnan on 2018/10/21.
 */
public class SpringUtils implements ApplicationContextAware {

    @Getter
    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    public static <T> T getBean(Class<T> requiredType) {
        return ctx.getBean(requiredType);
    }

}
