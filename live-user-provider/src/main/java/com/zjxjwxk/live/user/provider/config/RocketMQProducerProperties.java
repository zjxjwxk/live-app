package com.zjxjwxk.live.user.provider.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ生产者配置信息
 *
 * @author Xinkang Wu
 * @date 2025/4/26 13:30
 */
@ConfigurationProperties(prefix = "rocketmq.producer")
@Configuration
@Getter
@Setter
@ToString
public class RocketMQProducerProperties {

    /**
     * Nameserver地址
     */
    private String nameSrvAddr;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 消息重发次数
     */
    private int retryTimes;

    /**
     * 发送超时时间
     */
    private int sendTimeout;
}
