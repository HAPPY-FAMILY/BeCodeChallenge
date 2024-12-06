package com.code.ping.service

import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.time.Instant

/**
 * 测试限流方法是否正常
 */
//@SpringBootTest
@ActiveProfiles("test")
class RateLimiterServiceTest extends Specification  {

    private static final String LOCK_FILE = "/Users/macbook/Downloads/test_tmp/rate_limiter.lock";
    private static final String COUNTER_FILE = "/Users/macbook/Downloads/test_tmp/rate_counter.txt";
    private static final int MXA_LIMIT = 2; // 每秒允许的最大请求数

    RateLimiterService rateLimiterService = new RateLimiterService(LOCK_FILE, COUNTER_FILE, MXA_LIMIT)

    def setup() {

        // 清理测试环境
        def tempFolder = new File("/Users/macbook/Downloads/test_tmp")
        if (tempFolder.exists()) {
            tempFolder.listFiles().each { file ->
                file.delete()
            }
            tempFolder.delete()
        }
        if (!tempFolder.exists()) {
            // 文件夹不存在则创建
            tempFolder.mkdirs();
        }
    }

    def cleanup() {
        // 清理测试环境
        def tempFolder = new File("/Users/macbook/Downloads/test_tmp")
        if (tempFolder.exists()) {
            tempFolder.listFiles().each { file ->
                file.delete()
            }
            tempFolder.delete()
        }
    }

    // 允许获得文件锁
    def "Should acquire lock and allow request if not limited"() {
        given: "a mock for FileLock and FileChannel"
        def fileChannel = Mock(FileChannel)
        def fileLock = Mock(FileLock)
        def randomAccessFile = Mock(RandomAccessFile)

        // 模拟锁和文件读取
        fileChannel.lock() >> fileLock
        randomAccessFile.getChannel() >> fileChannel
        // 模拟没有被checkAndUpdateCounterFile限流返回true
        rateLimiterService.checkAndUpdateCounterFile(null)  >> true

        when: "tryAcquire is called"
        def result = rateLimiterService.tryAcquire().block()

        then: "The request is allowed"
        result == true
    }

    // 获取文件锁触发异常
    def "Should return error if IOException occurs during lock acquisition"() {
        given: "A mock for RandomAccessFile and FileChannel that throws IOException"
        // 删除文件夹，触发IOException验证
        def tempFolder = new File("/Users/macbook/Downloads/test_tmp")
        if (tempFolder.exists()) {
            tempFolder.listFiles().each { file ->
                file.delete()
            }
            tempFolder.delete()
        }

        when: "tryAcquire is called"
        def result = rateLimiterService.tryAcquire().onErrorReturn(false).block()

        then: "An error should be handled and false returned"
        result == false
    }

    // 测试写入文件
    def "Should write data to counter file"() {
        given: "Define timestamp and count"
        long timestamp = 1234567890L
        int count = 5
        def counterFile = new File(COUNTER_FILE)

        when: "WriteCounter is called"
        rateLimiterService.writeCounter(timestamp, count)

        then: "The file should contain the correct data"
        counterFile.exists()
        counterFile.text == "$timestamp,$count"
    }

    // 测试首次创建文件(同一秒内counter由0 + 1)
    def "Should initialize counter file if it does not exist"() {
        given: "new CounterFile"
        File counterFile = new File(COUNTER_FILE)

        when: "checkAndUpdateCounterFile is called"
        long currentTimestamp = Instant.now().getEpochSecond()
        boolean result = rateLimiterService.checkAndUpdateCounterFile(currentTimestamp)

        then: "A new counter file is created with initial values and result must be true"
        def content = counterFile.text
        def count = content.substring(content.lastIndexOf(",") + 1)
        result && count == "1"
    }

    // 测试同一秒内，未达到限制次数时自增1
    def "Should update counter if within the same second and under limit"() {
        given: "Write the current timestamp and under limit to counter file"
        long currentTimestamp = Instant.now().getEpochSecond()
        File counterFile = new File(COUNTER_FILE)
        // 写入自定义数据
        counterFile.text = "$currentTimestamp,1"

        when: "checkAndUpdateCounterFile is called"
        boolean result = rateLimiterService.checkAndUpdateCounterFile(currentTimestamp)

        then: "the counter is incremented and the request is allowed"
        result & counterFile.text == "$currentTimestamp,2"
    }

    // 测试同一秒内达到上限
    def "Should block request if within the same second and at limit"() {
        given: "Write the current timestamp and at limit to counter file"
        long currentTimestamp = Instant.now().getEpochSecond()
        File counterFile = new File(COUNTER_FILE)
        counterFile.text = "$currentTimestamp,$MXA_LIMIT"

        when: "checkAndUpdateCounterFile is called"
        boolean result = rateLimiterService.checkAndUpdateCounterFile(currentTimestamp)

        then: "the request is blocked and the counter file remains unchanged"
        !result
    }

    // 测试不同一秒内重置计数器
    def "Should reset counter if not in the same second"() {
        given: "Sleep 1 second and Write the define content to counter file"
        long currentTimestamp = Instant.now().getEpochSecond()
        File counterFile = new File(COUNTER_FILE)
        counterFile.text = "$currentTimestamp,5"

        when: "checkAndUpdateCounterFile is called"
        // 睡一秒，达到不是同一秒的条件
        sleep(1000)
        boolean result = rateLimiterService.checkAndUpdateCounterFile(null)

        then: "the counter reset and the request is allowed"
        def content = counterFile.text
        def count = content.substring(content.lastIndexOf(",") + 1)
        result && count == "1"
    }

}
