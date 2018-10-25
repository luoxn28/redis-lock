## redis-lock

基于redis的分布式锁实现

#### 如何使用

> 引入依赖

```xml
<dependency>
    <groupId>com.luo.redis.lock</groupId>
    <artifactId>redis-lock</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> redis配置

```yaml
spring:
  redis:
    host: 192.168.31.247
    port: 6379
    password: 123456
#    sentinel:
#      master: my-master
#      nodes: 192.168.31.247:6379,192.168.31.247:6379
```

> 代码示例

```java
@SpringBootApplication
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);

        // 锁获取
        RedisLock lock = new RedisLock(UUID.randomUUID().toString(), false);

        Assert.isTrue(lock.tryLock());
        Assert.isTrue(!lock.tryLock());
        Assert.isTrue(lock.unlock());
    }
}
```