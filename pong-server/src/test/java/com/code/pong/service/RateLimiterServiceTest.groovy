package com.code.pong.service

import spock.lang.Specification

import java.time.Duration

/**
 * 测试限流方法
 */
class RateLimiterServiceTest extends Specification {
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
        results == [true, false, false, false, true]
    }
}
