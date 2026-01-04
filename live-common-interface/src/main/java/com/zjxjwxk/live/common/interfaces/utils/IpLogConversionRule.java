package com.zjxjwxk.live.common.interfaces.utils;

import ch.qos.logback.core.PropertyDefinerBase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * IP -> Log 文件转换规则
 * 保证每个 Docker 容器的日志挂载目录唯一性
 *
 * @author Xinkang Wu
 * @date 2026/1/4 13:24
 */
public class IpLogConversionRule extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return getLogIndex();
    }

    private String getLogIndex() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return String.valueOf(ThreadLocalRandom.current().nextInt(100000));
        }
    }
}
