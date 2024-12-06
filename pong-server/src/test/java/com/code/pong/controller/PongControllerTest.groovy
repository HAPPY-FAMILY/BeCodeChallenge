package com.code.pong.controller

import jakarta.annotation.Resource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

/**
 * 测试远程调用pong是否触发限流
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PongControllerTest extends Specification {

    @Resource
    WebTestClient webTestClient

    // 测试请求5次并触发限流
    def "Should request pong /Hello endpoint 5 times"() {
        when: "Sending 5 requests to /pong/Hello with 300ms intervals"
        def results = []
        5.times {
            // 每次发送请求并等待响应
            def response = webTestClient.post().uri("/pong/Hello")
                    .exchange()
                    .expectBody(String.class)
                    .returnResult().responseBody
            // 收集响应结果
            results << response
            Thread.sleep(300) // 每次请求之间等待300毫秒
        }

        then: "Results match the expected outcomes"
        // 判断是否跟期望相同，初始化时间跟第一次的请求时间会相差1000多毫秒
        results.contains("World") && results.contains("Too Many Requests")
    }

    // 测试访问其他地址，并返回403
    def "Should request pong /Hi endpoint response 403"() {
        when: "Sending a requests to /pong/Hi"
        // 不按要求发送请求
        def response = webTestClient.post().uri("/pong/Hi")
                .exchange()
                .expectBody(String.class)
                .returnResult().getStatus().value()

        then: "The expected status is 403"
        response == 403
    }
}
