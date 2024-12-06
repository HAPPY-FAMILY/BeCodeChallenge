package com.code.ping.service

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.lang.reflect.Field

/**
 * 测试用于请求的sendPong
 */
@SpringBootTest
@ActiveProfiles("test")
class RequestServiceTest extends Specification {

    RequestService requestService

    // 模拟发送请求成功
    def "SendPong with status 200 for /pong/Hello"() {
        given: "A mock WebClient with a 200 and response 'World'"
        def mockWebClient = Mock(WebClient)
        def mockRequest = Mock(WebClient.RequestBodyUriSpec)
        def mockResponse = Mock(WebClient.ResponseSpec)
        def mockMono = Mono.just(ResponseEntity.ok("World"))

        mockWebClient.post() >> mockRequest
        mockRequest.uri("/pong/Hello") >> mockRequest
        mockRequest.retrieve() >> mockResponse
        mockResponse.bodyToMono(String.class) >> mockMono

        requestService = new RequestService()
        // 通过反射将mock WebClient注入私有字段
        Field field = RequestService.class.getDeclaredField("pongWebClient")
        field.setAccessible(true)
        field.set(requestService, mockWebClient)

        when: "Call sendPong with '/pong/Hello'"
        def result = requestService.sendPong("/pong/Hello").block()

        then: "The code should be 200 and the result should be 'World'"
        def response = ((ResponseEntity<String>)result);
        response.statusCode.value() == 200 && response.body == "World"
    }

    // 模拟发送请求成功，被Pong限流
    def "SendPong with status 429 for /pong/Hello"() {
        given: "A mock WebClient with a 429 and response 'Too Many Requests'"
        def mockWebClient = Mock(WebClient)
        def mockRequest = Mock(WebClient.RequestBodyUriSpec)
        def mockResponse = Mock(WebClient.ResponseSpec)
        def mockMono = Mono.just(ResponseEntity.status(429).body("Too Many Requests"));

        mockWebClient.post() >> mockRequest
        mockRequest.uri("/pong/Hello") >> mockRequest
        mockRequest.retrieve() >> mockResponse
        mockResponse.bodyToMono(String.class) >> mockMono

        requestService = new RequestService()
        // 通过反射将mock WebClient注入私有字段
        Field field = RequestService.class.getDeclaredField("pongWebClient")
        field.setAccessible(true)
        field.set(requestService, mockWebClient)

        when: "Call sendPong with '/pong/Hello'"
        def result = requestService.sendPong("/pong/Hello").block()

        then: "The code should be 429 and the result should be 'Too Many Requests'"
        def response = ((ResponseEntity<String>)result);
        response.statusCode.value() == 429 && response.body == "Too Many Requests"
    }
}
