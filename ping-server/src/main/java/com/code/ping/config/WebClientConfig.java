package com.code.ping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * WebClient配置
 */
@Configuration
public class WebClientConfig {

    @Value("${pong.url}")
    private String pingUrl;

    @Bean
    public WebClient pongWebClient() {
        // 配置连接池
        ConnectionProvider connectionProvider = ConnectionProvider.builder("pong-connection-provider")
                .maxConnections(5)
                .pendingAcquireMaxCount(10)
                .maxIdleTime(Duration.ofSeconds(30))
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(pingUrl)
                .build();
    }
}
