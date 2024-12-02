package com.code.ping.service;

import jakarta.annotation.Resource;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);


    // 自增计数器的KEY
    private static final String COUNTER_KEY = "limit_rate:counter";

    @Value("${rate-limit.ping.limit-for-period}")
    private int limitForPeriod;
    @Value("${rate-limit.ping.limit-refresh-period}")
    private Duration duration;

    public boolean tryAcquire(RedissonClient redissonClient, RLock lock) {
        try {
            // 尝试获取锁，设置锁的超时时间为5秒
            if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                // 获取 Redis 中的自增计数器
                RAtomicLong counter = redissonClient.getAtomicLong(COUNTER_KEY);

                // 自增计数器并在第一次操作时设置过期时间为1秒
                long currentCount = counter.incrementAndGet();
                if (currentCount == 1) {
                    counter.expire(duration);
                }
                // 检查当前计数器值是否超过限制
                return currentCount <= limitForPeriod;
            } else {
                logger.warn("Could not acquire lock, another instance is executing...");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
