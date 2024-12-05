package com.code.ping.service;

import com.alibaba.fastjson.JSON;
import com.code.ping.entity.Logs;
import com.code.ping.enums.PingLogsStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 日志队列封装
 */
@Service
public class LogsService {
    private static final Logger logger = LoggerFactory.getLogger(LogsService.class);

    private static final String LOGS_TOPIC = "logs-topic";

    @Value("${spring.application.name}")
    private String instance;
    @Value("${server.port}")
    private Integer port;

    private final RocketMQTemplate rocketMQTemplate;

    public LogsService(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送日志到队列，统一进行消费
     * @param pingLogsStatus ping日志枚举
     */
    public void sendPingLogs(PingLogsStatus pingLogsStatus) {
        Logs logs = new Logs(instance, port, pingLogsStatus.getStatus(), pingLogsStatus.getMessage());
        // 发送到队列
        rocketMQTemplate.convertAndSend(LOGS_TOPIC, JSON.toJSONString(logs));
    }
}
