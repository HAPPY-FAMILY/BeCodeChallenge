package com.code.ping.config

import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

/**
 * 测试RedissonClient注入
 */
@SpringBootTest
@ActiveProfiles("test")
class RedissonConfigTest extends Specification {

    @Autowired
    RedissonClient redissonClient

    def "redissonClient should be configured correctly"() {
        expect: "redissonClient is not null"
        redissonClient != null
    }
}
