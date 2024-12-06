package com.code.pong.consumer

import com.alibaba.fastjson2.JSON
import com.code.pong.entity.Logs
import com.code.pong.service.LogsService
import reactor.core.publisher.Mono
import spock.lang.Specification

/**
 * 模拟RocketMQ的监听方法
 */
class LogsConsumerTest extends Specification {

    LogsService logsService = Mock(LogsService)
    LogsConsumer logsConsumer = new LogsConsumer(logsService)

    // 模拟正常监听到消息
    def "Should save log successfully when receiving valid message"() {
        given: "A valid log message"
        String message = '{"instance":"mock_logs","ip":"127.0.0.1","port":8080,"status":200,"message":"Success","createTime":"2024-01-01T12:00:00"}'
        Logs logs = JSON.parseObject(message, Logs.class)

        when: "The message is received and processed"
        logsConsumer.onMessage(message) // 接收信息

        then: "The log is saved to the repository"
        logsService.saveLogs(_,_) >> Mono.just(logs)
    }

    // 模拟见听到消息后，保存异常
    def "Error when saving log fails"() {
        given: "A valid log message"
        String message = '{"instance":"mock_logs","ip":"127.0.0.1","port":8080,"status":200,"message":"Success","createTime":"2024-01-01T12:00:00"}'

        when: "The message is received and saving fails"
        logsService.saveLogs(_,_) >> Mono.error(new RuntimeException("Simulate saving log error"))
        logsConsumer.onMessage(message)

        then: "Log error is printed"
        println("Failed to save log:" + message)
    }
}
