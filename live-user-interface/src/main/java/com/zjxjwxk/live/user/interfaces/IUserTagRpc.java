package com.zjxjwxk.live.user.interfaces;

import com.zjxjwxk.live.user.constants.UserTagsEnum;

/**
 * @author Xinkang Wu
 * @date 2025/6/2 16:57
 */
public interface IUserTagRpc {

    /**
     * 设置标签
     */
    boolean setTag(Long userId, UserTagsEnum userTagsEnum);

    /**
     * 取消标签
     */
    boolean cancelTag(Long userId, UserTagsEnum userTagsEnum);

    /**
     * 是否包含某个标签
     */
    boolean containTag(Long userId, UserTagsEnum userTagsEnum);
}
