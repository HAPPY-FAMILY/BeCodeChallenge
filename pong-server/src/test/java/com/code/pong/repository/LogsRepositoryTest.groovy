package com.code.pong.repository

import com.code.pong.entity.Logs
import jakarta.annotation.Resource
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.LocalDateTime

/**
 * 测试保存到mongodb的方法
 */
@SpringBootTest
class LogsRepositoryTest extends Specification {

    @Resource
    LogsRepository logsRepository;

    def "Save data using MongoDB"() {
        given: "init logs entity"
        def logs = new Logs()
        logs.setId("1-mock-logs-id")
        logs.setInstance("mock_logs")
        logs.setPort(8080)
        logs.setStatus(200)
        logs.setMessage("Spock Test")
        logs.setCreateTime(LocalDateTime.now())

        when: "Save logs"
        def logEntity = logsRepository.save(logs).block()

        then: "logEntity is non-null"
        logEntity != null
    }
}
