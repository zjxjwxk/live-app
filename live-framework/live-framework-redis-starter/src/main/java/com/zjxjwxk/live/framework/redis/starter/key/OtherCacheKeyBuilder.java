package com.zjxjwxk.live.framework.redis.starter.key;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Conditional;

/**
 * @author Xinkang Wu
 * @date 2025/4/21 21:03
 */
@Configurable
@Conditional(RedisKeyLoadMatch.class)
public class OtherCacheKeyBuilder extends RedisKeyBuilder {

    private static final String USER_INFO_KEY = "other";

    public String buildUserInfoKey(Long userId) {
        return super.getPrefix() + USER_INFO_KEY + super.getSplitItem() + userId;
    }
}
