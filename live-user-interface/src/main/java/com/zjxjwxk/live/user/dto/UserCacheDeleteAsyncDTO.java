package com.zjxjwxk.live.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户缓存异步删除DTO
 *
 * @author Xinkang Wu
 * @date 2025/6/7 16:20
 */
@Getter
@Setter
public class UserCacheDeleteAsyncDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2938618868814778305L;

    /**
     * 缓存异步删除类型
     */
    private int code;
    private String json;
}
