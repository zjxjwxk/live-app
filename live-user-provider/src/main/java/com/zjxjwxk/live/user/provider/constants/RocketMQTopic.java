package com.zjxjwxk.live.user.provider.constants;

/**
 * RocketMQ Topic枚举
 *
 * @author Xinkang Wu
 * @date 2025/4/26 17:21
 */
public interface RocketMQTopic {

    /**
     * 异步删除用户相关缓存（用于更新后延迟双删）
     */
    String DELETE_USER_CACHE_ASYNC = "delete-user-cache";
}
