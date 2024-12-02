package com.code.ping

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

/**
 * 测试SpringBoot入口
 */
@SpringBootTest
@ActiveProfiles("test")
class PingServerApplicationTest extends Specification {

    def "Application context should load successfully"() {
        expect: "The application context loads without errors"
        true
    }
}
