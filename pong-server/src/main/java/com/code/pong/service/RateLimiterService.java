package com.code.pong.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimiterService {

    private final Semaphore semaphore;
    private final Duration refreshPeriod;
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    public RateLimiterService(@Value("${rate-limit.pong.limit-for-period}") int limitForPeriod,
                              @Value("${rate-limit.pong.limit-refresh-period}") Duration duration) {
        semaphore = new Semaphore(limitForPeriod);
        refreshPeriod = duration;
    }

    /**
     * semaphore tryAcquire, reset time
     * @return Mono<Boolean>
     */
    public Mono<Boolean> tryAcquire() {
        return Mono.defer(() -> {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastResetTime.get();
            // 如果超过 1 秒，更新重置时间并释放信号量
            if (currentTime - lastTime > refreshPeriod.getSeconds() * 1000) {
                if (lastResetTime.compareAndSet(lastTime, currentTime)) {
                    semaphore.release();
                }
            }
            // 尝试获取许可
            boolean acquired = semaphore.tryAcquire();
            return Mono.just(acquired);
        });
    }
}
