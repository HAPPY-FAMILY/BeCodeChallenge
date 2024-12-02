package com.code.ping.entity;



import java.time.LocalDateTime;

/**
 * 日志
 */
public class Logs {

    private String id;
    /** 产生日志的实例 */
    private String instance;
    /** 产生日志的实例端口 */
    private Integer port;
    /** 产生日志的实例ip */
    private String ip;
    /** 200:发送成功, 429:限流, 403:没有按格式发送Hello, 0:跨进程限流, 500:其他错误 */
    private Integer status;
    /** 成功或错误信息 */
    private String message;
    /** 创建时间 */
    private LocalDateTime createTime;

    public Logs() {}

    public Logs(String instance, Integer port, String ip, Integer status, String message) {
        this.instance = instance;
        this.port = port;
        this.ip = ip;
        this.status = status;
        this.message = message;
        this.createTime = LocalDateTime.now();
    }

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
