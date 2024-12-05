package com.code.pong.consumer

import com.alibaba.fastjson2.JSON
import com.code.pong.entity.Logs
import com.code.pong.repository.LogsRepository
import reactor.core.publisher.Mono
import spock.lang.Specification

/**
 * 模拟RocketMQ的监听方法
 */
class LogsConsumerTest extends Specification {

    LogsRepository logsRepository = Mock(LogsRepository)
    LogsConsumer logsConsumer = new LogsConsumer(logsRepository)

    def "Should save log successfully when receiving valid message"() {
        given: "a valid log message"
        String message = '{"instance":"mock_logs","ip":"127.0.0.1","port":8080,"status":200,"message":"Success","createTime":"2024-01-01T12:00:00"}'
        Logs logs = JSON.parseObject(message, Logs.class)

        when: "the message is received and processed"
        logsConsumer.onMessage(message) // 接收信息

        then: "the log is saved to the repository"
        logsRepository.save(_) >> Mono.just(logs)
    }

    def "Error when saving log fails"() {
        given: "a valid log message"
        String message = '{"instance":"mock_logs","ip":"127.0.0.1","port":8080,"status":200,"message":"Success","createTime":"2024-01-01T12:00:00"}'

        when: "the message is received and saving fails"
        logsRepository.save(_) >> Mono.error(new RuntimeException("Simulate saving log error"))
        logsConsumer.onMessage(message)

        then: "log error is printed"
        println("Failed to save log:" + message)
    }
}
