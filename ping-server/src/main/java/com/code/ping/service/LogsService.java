package com.code.ping.service;

import com.code.ping.entity.Logs;
import com.code.ping.enums.PingLogsStatus;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 日志队列封装
 */
@Service
public class LogsService {

    @Value("${spring.application.name}")
    private String instance;
    @Value("${server.port}")
    private Integer port;

    @Resource
    private ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * 保存日志到mongodb
     * @param pingLogsStatus
     * @return
     */
    public void saveLogs(PingLogsStatus pingLogsStatus, String collection) {
        Logs logs = new Logs(instance, port, pingLogsStatus.getStatus(), pingLogsStatus.getMessage());
        reactiveMongoTemplate.insert(logs, collection).subscribe();
    }
}
