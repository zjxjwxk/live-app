package com.zjxjwxk.live.user.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户缓存异步删除类型
 *
 * @author Xinkang Wu
 * @date 2025/6/7 16:23
 */
@Getter
@AllArgsConstructor
public enum UserCacheDeleteAsyncCode {

    USER_INFO(0, "用户信息"),
    USER_TAG(1, "用户标签");

    final int code;
    final String desc;
}
