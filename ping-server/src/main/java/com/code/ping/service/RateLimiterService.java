package com.code.ping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Instant;

@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    private final String LOCK_FILE;
    public final String COUNTER_FILE;
    public final int MXA_LIMIT; // 每秒允许的最大请求数


    public RateLimiterService(@Value("${rate-limit.ping.lock-file}") String lockFile,
                              @Value("${rate-limit.ping.counter-file}") String counterFile,
                              @Value("${rate-limit.ping.max-limit}") int maxLimit) {
        LOCK_FILE = lockFile;
        COUNTER_FILE = counterFile;
        MXA_LIMIT = maxLimit;

        // 加载时判断文件夹是否存在
        File tempFolder = new File(LOCK_FILE.substring(0, LOCK_FILE.lastIndexOf('/')));
        if (!tempFolder.exists()) {
            // 文件夹不存在则创建
            tempFolder.mkdirs();
        }
    }
    /**
     * 尝试进行限流操作
     *
     * @return 是否可以继续处理请求
     */
    public Mono<Boolean> tryAcquire() {
        return Mono.create(sink -> {
            try (RandomAccessFile lockFile = new RandomAccessFile(LOCK_FILE, "rw");
                 FileChannel channel = lockFile.getChannel();
                 FileLock fileLock = channel.lock()) { // 获取文件锁
                // 判断是否超过次数和更新计数器文件
                boolean allowed = checkAndUpdateCounterFile(null);
                sink.success(allowed);
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

    /**
     * 判断是否超过次数和更新计数器文件
     * @return 是否允许请求
     */
    public boolean checkAndUpdateCounterFile(Long currentSecond) throws IOException {
        File counterFile = new File(COUNTER_FILE);
        // 初始化文件，如果不存在
        if (!counterFile.exists()) {
            // 初始计数器内容
            writeCounter(Instant.now().getEpochSecond(), 0);
        }
        // 使用处理流读取文件内容
        String content;
        try (BufferedReader reader = new BufferedReader(new FileReader(counterFile))) {
            // 读取一整行
            content = reader.readLine();
        }

        // 解析内容
        String[] parts = content.split(",");
        long timestamp = Long.parseLong(parts[0]);
        int count = Integer.parseInt(parts[1]);
        // 获取秒级时间戳
        currentSecond = currentSecond == null ? Instant.now().getEpochSecond() : currentSecond;
        // 如果当前时间戳与文件的时间戳不一致
        if (currentSecond != timestamp) {
            // 新的一秒，重置计数器，覆盖文件内容
            writeCounter(currentSecond, 1);
            return true; // 新的一秒的第一次请求总是允许
        } else if (count < MXA_LIMIT) { // 仍在当前秒，且计数未达上限
            // 次数+1，覆盖文件内容
            writeCounter(timestamp, count + 1);
            return true;
        } else { // 超过次数进行限流
            return false;
        }
    }

    /**
     * 写入计数器文件
     *
     * @param timestamp 当前时间戳
     * @param count     当前计数
     */
    public void writeCounter(long timestamp, int count) throws IOException {
        try (FileWriter writer = new FileWriter(COUNTER_FILE, false)) {
            writer.write(timestamp + "," + count);
        }
    }
}
