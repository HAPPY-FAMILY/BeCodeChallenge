package com.code.pong.consumer;

import com.alibaba.fastjson2.JSON;
import com.code.pong.entity.Logs;
import com.code.pong.repository.LogsRepository;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "logs-topic", consumerGroup = "logs-group")
public class LogsConsumer implements RocketMQListener<String> {
    private static final Logger logger = LoggerFactory.getLogger(LogsConsumer.class);

    private final LogsRepository logsRepository;

    public LogsConsumer(LogsRepository logsRepository) {
        this.logsRepository = logsRepository;
    }

    @Override
    public void onMessage(String message) {
        Logs logs = JSON.parseObject(message, Logs.class);
        // 保存日志到mongodb
        logsRepository.save(logs)
                .doOnSuccess(log -> logger.info("Saved log success: {}", message))
                .doOnError(error -> logger.error("Failed to save log: {}, exception: {}", message, error.getMessage()))
                .subscribe();
    }
}
