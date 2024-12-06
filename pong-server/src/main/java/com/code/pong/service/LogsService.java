package com.code.pong.service;

import com.code.pong.entity.Logs;
import jakarta.annotation.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LogsService {

    @Resource
    private ReactiveMongoTemplate reactiveMongoTemplate;


    public Mono<Logs> saveLogs(Logs logs, String collectionName) {
        return reactiveMongoTemplate.save(logs, collectionName);
    }
}
