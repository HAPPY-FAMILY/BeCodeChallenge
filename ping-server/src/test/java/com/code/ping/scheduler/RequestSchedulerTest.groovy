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

    private static final String LOGS_COLLECTION = "logs"
    private static final String LOCK_FILE = "/Users/macbook/Downloads/test_tmp/rate_limiter.lock";
    private static final String COUNTER_FILE = "/Users/macbook/Downloads/test_tmp/rate_counter.txt";
    private static final int MXA_LIMIT = 2; // 每秒允许的最大请求数
    RateLimiterService rateLimiterService = new RateLimiterService(LOCK_FILE, COUNTER_FILE, MXA_LIMIT)

    def requestService = Mock(RequestService)
    def logsService = Mock(LogsService)

    @Subject
    def scheduler = new RequestScheduler(requestService, logsService, rateLimiterService)

    // 测试5次检查是否触发限流
    def "should execute 5 times with expected results"() {
        given: "Define empty arrays"
        def results = [] // 用于存储结果

        when: "Execute 5 times"
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

    // 测试发送成功后保存成功日志
    def "Should save success log when requestService sends pong successfully"() {
        given: "Simulate tryAcquire and sendPong to return"
        // 模拟 rateLimiterService.tryAcquire() 返回 true
        rateLimiterService.tryAcquire() >> Mono.just(true)
        // 模拟 requestService.sendPong() 返回成功响应
        requestService.sendPong("/Hello") >> Mono.just("World")

        when: "Execute is called"
        sleep(1000)
        scheduler.execute()

        then: "Save logs"
        1 * logsService.saveLogs(PingLogsStatus.SUCCESS, LOGS_COLLECTION) // 验证成功日志保存
        0 * logsService.saveLogs(PingLogsStatus.PONG_LIMIT, LOGS_COLLECTION) // 没有限流日志
        0 * logsService.saveLogs(PingLogsStatus.PING_LIMIT, LOGS_COLLECTION) // 没有 Ping 限流日志
    }

    // 测试发送后限流 保存限流日志
    def "Should save pong limit log when pong is rate limited"() {
        given:
        // 模拟 rateLimiterService.tryAcquire() 返回 true
        rateLimiterService.tryAcquire() >> Mono.just(true)
        // 模拟 requestService.sendPong() 抛出 429 错误
        requestService.sendPong("/Hello") >> Mono.error(WebClientResponseException.create(
                429, "Too Many Requests", null, null, null))

        when:
        sleep(1000)
        scheduler.execute()

        then:
        1 * logsService.saveLogs(PingLogsStatus.PONG_LIMIT, LOGS_COLLECTION) // 验证限流日志保存
        0 * logsService.saveLogs(PingLogsStatus.SUCCESS, LOGS_COLLECTION) // 没有成功日志
        0 * logsService.saveLogs(PingLogsStatus.PING_LIMIT, LOGS_COLLECTION) // 没有 Ping 限流日志
    }

    // 测试发送后触发其他WebClientResponseException
    def "Should print other WebClient exception log when pong is rate limited"() {
        given:
        // 模拟 rateLimiterService.tryAcquire() 返回 true
        rateLimiterService.tryAcquire() >> Mono.just(true)
        // 模拟 requestService.sendPong() 抛出 403 错误
        requestService.sendPong("/Hello") >> Mono.error(WebClientResponseException.create(
                403, "Illegal parameter.", null, null, null))

        when:
        sleep(1000)
        scheduler.execute()

        then:
        0 * logsService.saveLogs(PingLogsStatus.PONG_LIMIT, LOGS_COLLECTION) // 验证限流日志保存
        0 * logsService.saveLogs(PingLogsStatus.SUCCESS, LOGS_COLLECTION) // 没有成功日志
        0 * logsService.saveLogs(PingLogsStatus.PING_LIMIT, LOGS_COLLECTION) // 没有 Ping 限流日志
    }

    // 测试触发的其他异常
    def "Should handle other errors"() {
        given:
        // 模拟 rateLimiterService.tryAcquire() 返回 true
        rateLimiterService.tryAcquire() >> Mono.just(true)
        // 模拟 requestService.sendPong() 抛出其他错误
        requestService.sendPong("/Hello") >> Mono.error(new RuntimeException("Other error"))

        when:
        sleep(1000)
        scheduler.execute()

        then:
        0 * logsService.saveLogs(PingLogsStatus.SUCCESS, LOGS_COLLECTION) // 没有成功日志
        0 * logsService.saveLogs(PingLogsStatus.PONG_LIMIT, LOGS_COLLECTION) // 没有限流日志
        0 * logsService.saveLogs(PingLogsStatus.PING_LIMIT, LOGS_COLLECTION) // 没有 Ping 限流日志
    }

    // 测试触发进程限流
    def "Should handle ping limit errors"() {
        given: "Simulate tryAcquire and sendPong to return"
        // 模拟 rateLimiterService.tryAcquire() 返回 false
        rateLimiterService.tryAcquire() >> Mono.just(false)
        // 模拟 requestService.sendPong() 返回成功响应
        requestService.sendPong("/Hello") >> Mono.just("World")

        when:
        sleep(1000)
        3.times {
            scheduler.execute()
        }
        then:
        1 * logsService.saveLogs(PingLogsStatus.PING_LIMIT, LOGS_COLLECTION) // 没有 Ping 限流日志
        2 * logsService.saveLogs(PingLogsStatus.SUCCESS, LOGS_COLLECTION) // 没有成功日志
        0 * logsService.saveLogs(PingLogsStatus.PONG_LIMIT, LOGS_COLLECTION) // 没有限流日志
    }

}
