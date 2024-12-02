package com.code.ping.scheduler

import com.code.ping.enums.PingLogsStatus
import com.code.ping.service.LogsService
import com.code.ping.service.RateLimiterService
import com.code.ping.service.RequestService
import jakarta.annotation.Resource
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification

/**
 * 测试每秒调用pong
 */
@SpringBootTest
@ActiveProfiles("test")
class RequestSchedulerTest extends Specification {

    @Resource
    RateLimiterService rateLimiterService
    @Resource
    private RedissonClient redissonClient

    def requestService = Mock(RequestService)
    def logsService = Mock(LogsService)

    def requestScheduler

    def setup() {
        requestScheduler = new RequestScheduler(requestService, logsService, rateLimiterService, redissonClient)
    }

    def "should handle network error correctly and return error response"() {
        given: "Mock requestService to return a Mono.error with WebClientResponseException"
        requestService.sendPong("/Hello") >> Mono.error(
                WebClientResponseException.create(
                        500,
                        "Internal Server Error",
                        null,
                        null,
                        null
                )
        )

        when: "execute is called"
        def result = requestScheduler.execute().block()

        then: "result should be 'error'"
        result == "error"
    }

    def "should execute 5 times with expected results"() {
        given: "RequestService behavior is mocked"
        // 成功时返回 "World"
        requestService.sendPong("/Hello") >> Mono.just("World")

        when: "Execute method is called in a loop"
        Thread.sleep(1000)
        def results = []
        5.times {
            def result = requestScheduler.execute().block() // 执行并获取结果
            results << result
            // 根据结果模拟调用发送日志方法
            if (result == "World") {
                logsService.sendPingLogs(PingLogsStatus.SUCCESS)
            } else if (result == "limit") {
                logsService.sendPingLogs(PingLogsStatus.PING_LIMIT)
            }

            sleep(400) // 每次调用间隔400毫秒
        }
        then: "Results should match the expected outcome"
        results == ["World", "World", "limit", "World", "World"]
    }
}
