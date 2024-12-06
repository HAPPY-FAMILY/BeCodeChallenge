package com.code.pong.service

import spock.lang.Specification

import java.lang.reflect.Field
import java.time.Duration
import java.util.concurrent.Semaphore

/**
 * 测试限流方法
 */
class RateLimiterServiceTest extends Specification {
    // 测试限流并返回期望结果
    def "Should acquire permits with expected results"() {
        given: "A rate limiter with a limit period and refresh period"
        def rateLimiter = new RateLimiterService(1, Duration.ofSeconds(1))

        when: "tryAcquire is called in a loop with intervals"
        def results = []
        5.times {
            results << rateLimiter.tryAcquire().block() // 阻塞方式调用以获取结果
            Thread.sleep(300) // 每次调用间隔300毫秒
        }

        then: "Results match the expected outcomes"
        results.contains(true) && results.contains(false)
    }

    // 大于1秒，释放信号量
    def "Should trigger release logic when over 1 second"() {
        given: "A rate limiter instance"
        def rateLimiter = new RateLimiterService(1, Duration.ofSeconds(1))

        Field semaphoreField = RateLimiterService.class.getDeclaredField("semaphore")
        semaphoreField.setAccessible(true)
        def semaphore = (Semaphore)semaphoreField.get(rateLimiter)

        when: "RefreshIfNeeded is call"
        def currentMillis = System.currentTimeMillis()
        rateLimiter.releaseIfNeeded(currentMillis, currentMillis - 2000)

        then: "Semaphore should be released"
        semaphore.availablePermits() == 1
    }

    // 小于一秒，不释放信号量
    def "Should not trigger release logic when less 1 second"() {
        given: "A rate limiter instance"
        def rateLimiter = new RateLimiterService(1, Duration.ofSeconds(1))

        Field semaphoreField = RateLimiterService.class.getDeclaredField("semaphore")
        semaphoreField.setAccessible(true)
        def semaphore = (Semaphore)semaphoreField.get(rateLimiter)

        when: "Execute tryAcquire and RefreshIfNeeded is call"
        semaphore.tryAcquire()
        def currentMillis = System.currentTimeMillis()
        rateLimiter.releaseIfNeeded(currentMillis, currentMillis - 100)

        then: "Semaphore should not be released"
        semaphore.availablePermits() == 0
    }
}
