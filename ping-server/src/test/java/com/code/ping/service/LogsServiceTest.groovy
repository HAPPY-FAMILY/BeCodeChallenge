package com.code.ping.service

import com.code.ping.entity.Logs
import com.code.ping.enums.PingLogsStatus
import jakarta.annotation.Resource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import spock.lang.Specification

/**
 * 测试日志发送到MQ
 */
@SpringBootTest
class LogsServiceTest extends Specification {

    @Resource
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Resource
    LogsService logsService

    // 测试真实保存日志
    def "Save logs should call insert on ReactiveMongoTemplate"() {
        given: "new Logs and "
        def logs = new Logs("spock-test", 8080, PingLogsStatus.SUCCESS.getStatus(), PingLogsStatus.SUCCESS.getMessage())

        when: "ReactiveMongoTemplate.insert"
        def logEntity = reactiveMongoTemplate.insert(logs, "test-logs").block()

        then: "logEntity is non-null"
        logEntity != null
    }

    // 测试保存日志
    def "Save logs should call saveLogs"() {
        when: "SaveLogs is called"
        logsService.saveLogs(PingLogsStatus.SUCCESS, "test-logs")

        then: "No exception and save successfully"
        noExceptionThrown()
    }

}
