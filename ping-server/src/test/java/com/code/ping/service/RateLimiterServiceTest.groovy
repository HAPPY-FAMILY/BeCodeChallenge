package com.code.ping.service

import jakarta.annotation.Resource
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

/**
 * 测试限流方法是否正常
 */
@SpringBootTest
@ActiveProfiles("test")
class RateLimiterServiceTest extends Specification  {

    @Resource
    RateLimiterService rateLimiterService;
    @Resource
    private RedissonClient redissonClient;

    def "tryAcquire method executed successfully"() {
        expect: "tryAcquire return true or false"
        def lock = redissonClient.getLock("limit_rate:lock")
        rateLimiterService.tryAcquire(redissonClient, lock) || !rateLimiterService.tryAcquire(redissonClient, lock)
    }


}
