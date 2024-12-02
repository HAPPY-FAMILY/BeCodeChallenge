package com.code.ping.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP工具类
 */
public class IpUtils {

    public static String getLocalIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
