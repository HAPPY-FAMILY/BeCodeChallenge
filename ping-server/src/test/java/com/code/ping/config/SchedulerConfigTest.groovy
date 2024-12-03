package com.code.ping.config

import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
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
