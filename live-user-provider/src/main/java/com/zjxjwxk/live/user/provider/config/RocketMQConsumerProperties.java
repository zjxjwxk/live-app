package com.zjxjwxk.live.user.provider.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xinkang Wu
 * @date 2025/4/26 13:54
 */
@ConfigurationProperties(prefix = "rocketmq.consumer")
@Configuration
@Getter
@Setter
@ToString
public class RocketMQConsumerProperties {

    /**
     * Nameserver地址
     */
    private String nameSrvAddr;

    /**
     * 分组名称
     */
    private String groupName;
}
