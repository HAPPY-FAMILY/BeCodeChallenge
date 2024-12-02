package com.code.ping.config

import jakarta.annotation.Resource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

/**
 * 测试WebClient注入
 */
@SpringBootTest
@ActiveProfiles("test")
class WebClientConfigTest extends Specification {

    @Resource(name = "pongWebClient")
    WebClient webClient

    def "pongWebClient should return a WebClient instance"() {
        expect: "pongWebClient is not null"
        webClient != null
    }
}
