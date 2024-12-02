package com.code.pong.entity;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 日志
 */
@Document(collection = "logs")
@CompoundIndexes({
        @CompoundIndex(name = "instance_port_ip_index", def = "{'instance': 1, 'port': 1, 'ip': 1}")
})
public class Logs {
    @Id
    @Indexed
    private String id;
    /** 产生日志的实例 */
    private String instance;
    /** 产生日志的实例端口 */
    private Integer port;
    /** 产生日志的实例ip */
    private String ip;
    /** 200:发送成功, 429:限流, 403:没有按格式发送Hello, 0:跨进程限流, 500:其他错误 */
    @Indexed
    private Integer status;
    /** 成功或错误信息 */
    private String message;
    /** 创建时间 */
    @Indexed
    private LocalDateTime createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
