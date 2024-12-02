package com.code.ping.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 请求服务
 */
@Service
public class RequestService {

    @Resource(name = "pongWebClient")
    private WebClient pongWebClient;

    /**
     * 封装请求pong的方法
     * @param uri pong endpoint
     * @return Mono<String>
     */
    public Mono<String> sendPong(String uri) {
        return pongWebClient.post()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class);
    }
}
