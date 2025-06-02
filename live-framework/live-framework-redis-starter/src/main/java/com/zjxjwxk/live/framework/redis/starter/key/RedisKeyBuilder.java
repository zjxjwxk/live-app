package com.zjxjwxk.live.framework.redis.starter.key;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Xinkang Wu
 * @date 2025/4/21 19:16
 */
public class RedisKeyBuilder {

    @Value("${spring.application.name}")
    private String applicationName;
    private static final String SPLIT_ITEM = ":";

    public String getSplitItem() {
        return SPLIT_ITEM;
    }

    public String getPrefix() {
        return applicationName + SPLIT_ITEM;
    }
}
