package com.code.ping.config

import jakarta.annotation.Resource
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class SchedulerConfigTest extends Specification {

    @Resource
    SchedulerConfig schedulerConfig

    def "Should not load SchedulerConfig in test profile"() {
        expect:
        schedulerConfig != null
    }
}
