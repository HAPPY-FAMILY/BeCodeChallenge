package com.code.pong.repository;

import com.code.pong.entity.Logs;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogsRepository extends ReactiveMongoRepository<Logs, String> {
}
