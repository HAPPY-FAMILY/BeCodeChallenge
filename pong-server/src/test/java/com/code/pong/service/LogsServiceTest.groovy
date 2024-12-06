package com.code.pong.service

import com.code.pong.entity.Logs
import jakarta.annotation.Resource
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.LocalDateTime

/**
 * 测试保存到mongodb的方法
 */
@SpringBootTest
class LogsServiceTest extends Specification {

    @Resource
    LogsService logsService;

    def "Save data using MongoDB"() {
        given: "init logs entity"
        def logs = new Logs()
        logs.setInstance("mock_logs")
        logs.setPort(8080)
        logs.setStatus(200)
        logs.setMessage("Spock Test")
        logs.setCreateTime(LocalDateTime.now())

        when: "Save logs"
        def logEntity = logsService.saveLogs(logs, "test-logs").block()

        then: "logEntity is non-null"
        logEntity != null
    }
}
