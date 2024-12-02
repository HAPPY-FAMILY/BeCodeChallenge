package com.code.ping.enums;

/**
 * ping日志状态枚举
 */
public enum PingLogsStatus {

    /** 请求成功 */
    SUCCESS(200, "Ping sent Hello successfully, and Pong’s response was World."),
    /** PONG限流 */
    PONG_LIMIT(429, "Pong limit reached, please try again later."),
    /** PING限流 */
    PING_LIMIT(0, "Ping request was rate limited."),
    /** 非法参数 */
    PONG_FORBIDDEN(403, "Illegal parameter"),
    /** 未知错误 */
    UNKNOWN_ERROR(500, "Unknown error"),
    ;

    private Integer status;
    private String message;

    PingLogsStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
