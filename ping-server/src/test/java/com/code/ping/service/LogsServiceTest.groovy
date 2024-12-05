package com.code.ping.service

import com.alibaba.fastjson2.JSON
import com.code.ping.entity.Logs
import com.code.ping.enums.PingLogsStatus
import org.apache.rocketmq.spring.core.RocketMQTemplate
import spock.lang.Specification
import spock.lang.Subject

/**
 * 测试日志发送到MQ
 */
class LogsServiceTest extends Specification {

    RocketMQTemplate rocketMQTemplate

    @Subject
    LogsService logsService

    def setup() {
        rocketMQTemplate = Mock(RocketMQTemplate)
        logsService = new LogsService(rocketMQTemplate) // 将RocketMQTemplate通过构造器注入
    }

    def "should send logs successfully when sendPingLogs is called"() {
        given: "a PingLogsStatus"
        def logs = new Logs("ping", 8080, PingLogsStatus.SUCCESS.getStatus(), PingLogsStatus.SUCCESS.getMessage())

        // 模拟 RocketMQTemplate 的行为，验证发送消息
        rocketMQTemplate.convertAndSend(_ as String, _) >> { String topic, String message ->
            assert topic == "logs-topic"  // 验证正确的 topic
            assert message != null  // 验证消息内容
        }
        // 验证convertAndSend发送
        when: "the message should be sent successfully"
        rocketMQTemplate.convertAndSend("logs-topic", JSON.toJSONString(logs))

        // 验证sendPingLogs发送
        then: "sendPingLogs is called and should be sent successfully"
        logsService.sendPingLogs(PingLogsStatus.SUCCESS)
    }
}
