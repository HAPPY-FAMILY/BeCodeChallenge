package com.code.pong.controller;

import com.code.pong.service.RateLimiterService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pong")
public class PongController {

    private static final Logger logger = LoggerFactory.getLogger(PongController.class);

    @Resource
    private RateLimiterService rateLimiterService;

    /**
     * pong
     * @return Mono<ResponseEntity<String>>
     */
//    @RateLimiter(name = "pongRateLimiter", fallbackMethod = "fallback") // use resilience
    @PostMapping("/{says}")
    public Mono<ResponseEntity<String>> pong(@PathVariable("says") String says) {
        return rateLimiterService.tryAcquire().flatMap(acquire -> {
                if (!"Hello".equals(says)) {
                    return Mono.just(ResponseEntity.status(403).body("Request Forbidden"));
                }
                if (acquire) {
                    return Mono.just(ResponseEntity.ok("World"));
                }
                logger.error("Too Many Requests");
                return Mono.just(ResponseEntity.status(429).body("Too Many Requests"));
            });
    }

//    /**
//     * resilience fallback
//     * @param throwable throwable
//     * @return Mono<ResponseEntity<String>>
//     */
//    public Mono<ResponseEntity<String>> fallback(Throwable throwable) {
//        logger.error("fallback exception: {}", throwable.getMessage());
//        return Mono.just(ResponseEntity.status(429).body("Too Many Requests"));
//    }
}
