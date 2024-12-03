package com.code.ping.scheduler;

import com.code.ping.enums.PingLogsStatus;
import com.code.ping.service.LogsService;
import com.code.ping.service.RateLimiterService;
import com.code.ping.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Pong定时任务
 */
@Component
public class RequestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RequestScheduler.class);

    private static final String LOGS_COLLECTION = "logs";

    private final RequestService requestService;
    private final LogsService logsService;
    private final RateLimiterService rateLimiterService;

    public RequestScheduler(RequestService requestService, LogsService logsService, RateLimiterService rateLimiterService) {
        this.requestService = requestService;
        this.logsService = logsService;
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * 约1秒执行一次
     * 每秒请求pong，1秒最多请求2次
     */
    @Scheduled(fixedRate = 1000)
    public void execute() {
        rateLimiterService.tryAcquire()
                .flatMap(allowed -> {
                    if (allowed) {
                        return requestService.sendPong("/Hello")
                                .doOnSuccess(x -> { // 成功回调
                                    logger.info("Ping sent Hello successfully");
                                    // 保存日志到Mongo
                                    logsService.saveLogs(PingLogsStatus.SUCCESS, LOGS_COLLECTION);
                                })
                                .doOnError(e -> { // 错误回调
                                    if (e instanceof WebClientResponseException webClientException) {
                                        int statusCode = webClientException.getStatusCode().value();
                                        if (statusCode == PingLogsStatus.PONG_LIMIT.getStatus()) { // 判断状态码为429时发送日志队列
                                            logger.warn("Pong was rate limited.");
                                            // 保存日志到Mongo
                                            logsService.saveLogs(PingLogsStatus.PONG_LIMIT, LOGS_COLLECTION);
                                        } else { // 其他状态直接打印
                                            logger.error("Response error: {}", e.getMessage());
                                        }
                                    } else { // 其他非响应错误直接打印
                                        logger.error("Other error: {}", e.getMessage());
                                    }
                                }).onErrorReturn("error"); // 如果发生错误，返回 "error"
                    } else {
                        logger.warn("Ping request was rate limited.");
                        // 保存日志到Mongo
                        logsService.saveLogs(PingLogsStatus.PING_LIMIT, LOGS_COLLECTION);
                        return Mono.empty();
                    }
                }).subscribe();
    }
}
