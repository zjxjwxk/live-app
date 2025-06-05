package com.zjxjwxk.live.user.utils;

/**
 * 用户标签工具类
 *
 * @author Xinkang Wu
 * @date 2025/6/2 18:09
 */
public class UserTagUtils {

    /**
     * 判断是否存在某个标签
     * @param userTagInfo 用户当前标签信息
     * @param matchTag 需判断是否存在的标签
     * @return 是否存在该标签
     */
    public static boolean isContain(Long userTagInfo, Long matchTag) {
        return userTagInfo != null && matchTag != null && matchTag > 0 && (userTagInfo & matchTag) == matchTag;
    }
}
