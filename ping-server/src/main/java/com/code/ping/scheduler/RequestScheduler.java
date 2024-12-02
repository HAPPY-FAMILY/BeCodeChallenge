package com.code.ping.scheduler;

import com.code.ping.enums.PingLogsStatus;
import com.code.ping.service.LogsService;
import com.code.ping.service.RateLimiterService;
import com.code.ping.service.RequestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Pong定时任务
 */
@Component
public class RequestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RequestScheduler.class);
    // 分布式锁的KEY
    private static final String LOCK_KEY = "limit_rate:lock";

    private final RedissonClient redissonClient;
    private final RequestService requestService;
    private final LogsService logsService;
    private final RateLimiterService rateLimiterService;

    public RequestScheduler(RequestService requestService, LogsService logsService, RateLimiterService rateLimiterService, RedissonClient redissonClient) {
        this.requestService = requestService;
        this.logsService = logsService;
        this.rateLimiterService = rateLimiterService;
        this.redissonClient = redissonClient;
    }

    /**
     * 根据需求约1秒执行一次
     * 每秒请求pong，1秒最多请求2次
     */
    @Scheduled(fixedRate = 1020)
    public Mono<String> execute() {
        // 获取分布式锁
        RLock lock = redissonClient.getLock(LOCK_KEY);
        try {
            if (rateLimiterService.tryAcquire(redissonClient, lock)) { // 获取到令牌
                return requestService.sendPong("/Hello")
                        .doOnSuccess(x -> { // 成功回调
                            logger.info("Ping sent Hello successfully, and Pong’s response was World.");
                            // 发送日志到队列
                            logsService.sendPingLogs(PingLogsStatus.SUCCESS);
                        })
                        .doOnError(e -> { // 错误回调
                            if (e instanceof WebClientResponseException webClientException) {
                                int statusCode = webClientException.getStatusCode().value();
                                if (statusCode == PingLogsStatus.PONG_LIMIT.getStatus()) { // 判断状态码为429时发送日志队列
                                    logger.warn("Pong was rate limited.");
                                    logsService.sendPingLogs(PingLogsStatus.PONG_LIMIT);
                                } else { // 其他状态直接打印
                                    logger.error("Response error: {}", e.getMessage());
                                }
                            } else { // 其他非响应错误直接打印
                                logger.error("Unknown error: {}", e.getMessage());
                            }
                        }).onErrorReturn("error"); // 如果发生错误，返回 "error"
            } else { // 获取不到令牌
                logger.warn("Ping request was rate limited.");
                logsService.sendPingLogs(PingLogsStatus.PING_LIMIT);
                return Mono.just("limit");
            }
        } finally {
            lock.unlock();
        }

    }



//    private static final String LOCK_FILE_PATH = "/Users/macbook/Downloads/rate-limit.lock";
//    private static final String LOCK_FILE_COUNT = "lock_file_count";
//    @Resource
//    private StringRedisTemplate stringRedisTemplate;
//
//    /**
//     * 使用文件锁实现，redis作为所有实例的时间窗口，限制1秒最多发送2次请求
//     */
//    @Scheduled(fixedRate = 1020)
//    public void executeByFileLock() {
//        File lockFile = new File(LOCK_FILE_PATH);
//        if (!lockFile.exists()) {
//            try {
//                lockFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//                logger.error("锁文件生成失败");
//            }
//        }
//
//        FileOutputStream fos = null;
//        FileChannel channel = null;
//        FileLock tryLock = null;
//        try  {
//            fos = new FileOutputStream(LOCK_FILE_PATH);
//            channel = fos.getChannel();
//            tryLock = channel.tryLock();
//            if (tryLock != null) {
//                long count = stringRedisTemplate.opsForValue().increment(LOCK_FILE_COUNT);
//                if (count == 1) {
//                    stringRedisTemplate.expire(LOCK_FILE_COUNT, Duration.ofSeconds(1));
//                }
//                if (count > 2) {
//                    logger.warn("Ping request was rate limited.");
//                    logsService.sendPingLogs(PingLogsStatus.PING_LIMIT);
//                    return; // 限制处理
//                }
//
//                // 订阅 Mono，触发执行
//                requestService.sendPong("/Hello")
//                        .doOnSuccess(x -> { // 成功回调
//                            logger.info("Ping sent Hello successfully, and Pong’s response was World.");
//                            // 发送日志到队列
//                            logsService.sendPingLogs(PingLogsStatus.SUCCESS);
//                        })
//                        .doOnError(e -> { // 错误回调
//                            if (e instanceof WebClientResponseException webClientException) {
//                                int statusCode = webClientException.getStatusCode().value();
//                                if (statusCode == PingLogsStatus.PONG_LIMIT.getStatus()) { // 判断状态码为429时发送日志队列
//                                    logger.warn("Pong was rate limited.");
//                                    logsService.sendPingLogs(PingLogsStatus.PONG_LIMIT);
//                                } else { // 其他状态直接打印
//                                    logger.error("Response error: {}", e.getMessage());
//                                }
//                            } else { // 其他非响应错误直接打印
//                                logger.error("Unknown error: {}", e.getMessage());
//                            }
//                        }).subscribe();
//            } else {
//                logger.warn("Ping request was rate limited.");
//                logsService.sendPingLogs(PingLogsStatus.PING_LIMIT);
//            }
//        } catch (OverlappingFileLockException e) {
//            logger.warn("File is already locked");
//            logsService.sendPingLogs(PingLogsStatus.PING_LIMIT);
//        } catch (IOException e) {
//            logger.error("Unknown error: {}", e.getMessage());
//        } finally {
//            if (tryLock != null) {
//                try {
//                    tryLock.release();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (channel != null) {
//                try {
//                    channel.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }
}
