package com.code.ping.scheduler

import com.code.ping.enums.PingLogsStatus
import com.code.ping.service.LogsService
import com.code.ping.service.RateLimiterService
import com.code.ping.service.RequestService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Subject

/**
 * 测试每秒调用pong
 */
@SpringBootTest
@ActiveProfiles("test")
class RequestSchedulerTest extends Specification {

    private static final String LOCK_FILE = "/Users/macbook/Downloads/test_tmp/rate_limiter.lock";
    private static final String COUNTER_FILE = "/Users/macbook/Downloads/test_tmp/rate_counter.txt";
    private static final int MXA_LIMIT = 2; // 每秒允许的最大请求数
    RateLimiterService rateLimiterService = new RateLimiterService(LOCK_FILE, COUNTER_FILE, MXA_LIMIT)

    def requestService = Mock(RequestService)
    def logsService = Mock(LogsService)

    @Subject
    def scheduler = new RequestScheduler(requestService, logsService, rateLimiterService)


    // 测试Pong限流错误(429)
    def "Should handle 429 error and return error response"() {
        given: "Mock requestService to return a Mono.error with WebClientResponseException(429)"
        requestService.sendPong("/Hello") >> Mono.error(
                WebClientResponseException.create(
                        429,
                        "Simulate Too Many Requests",
                        null,
                        null,
                        null
                )
        )
        when: "execute is called"

        def result = scheduler.execute().block()

        then: "Result should be 'error'"
        result == "error"
    }


    // 测试网络错误
    def "Should handle network error correctly and return error response"() {
        given: "Mock requestService to return a Mono.error with WebClientResponseException"
        requestService.sendPong("/Hello") >> Mono.error(
                WebClientResponseException.create(
                        500,
                        "Simulate Internal Server Error",
                        null,
                        null,
                        null
                )
        )
        when: "execute is called"
        sleep(1000)
        def result = scheduler.execute().block()

        then: "Result should be 'error'"
        result == "error"
    }


    // 测试其他错误错误（这里自定义空指针异常）
    def "Should handle custom NullPointerException correctly and return error response"() {
        given: "Mock requestService to return a Mono.error with NullPointerException"
        requestService.sendPong("/Hello") >> Mono.error(
                new NullPointerException("Simulate Null Pointer Exception")
        )

        when: "Calling the service method that interacts with requestService"
        sleep(1000)
        def result = scheduler.execute().block()

        then: "An error response should be returned"
        result == "error"
    }


    // 测试5次检查是否触发限流
    def "Should execute 5 times with expected results By tryAcquire method"() {
        given: "Define empty arrays"
        def results = [] // 用于存储结果

        when: "Execute 5 times"
        sleep(1000)
        // 使用 5.times 调用 tryAcquire 并收集结果
        5.times {
            results << rateLimiterService.tryAcquire().block() // 调用并同步收集结果
            Thread.sleep(300) // 模拟每次调用的时间间隔
        }
        then: "The results contains true and false"
        // 验证结果集合
        assert results.contains(true) // 验证集合包含 true
        assert results.contains(false) // 验证集合包含 false（如果限流生效）
    }

    // 测试5次返回限流的期望值，跟上面差不多，这个模拟了远程请求的结果
    def "Should execute 5 times with expected results"() {
        given: "RequestService behavior is mocked"
        // 模拟成功时返回 "World"
        requestService.sendPong("/Hello") >> Mono.just("World")

        when: "Execute method is called in a loop"
        sleep(1000)
        def results = []
        5.times {
            def result = scheduler.execute().block() // 执行并获取结果
            results << result
            // 根据结果模拟调用发送日志方法
            if (result == "World") {
                logsService.sendPingLogs(PingLogsStatus.SUCCESS)
            } else if (result == "limit") {
                logsService.sendPingLogs(PingLogsStatus.PING_LIMIT)
            }
            sleep(300) // 模拟每次调用的时间间隔
        }
        then: "Results should match the expected outcome"
        assert results.contains("World")
        assert results.contains("limit")
    }
}
