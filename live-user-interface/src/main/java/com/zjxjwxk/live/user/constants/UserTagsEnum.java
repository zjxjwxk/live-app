package com.zjxjwxk.live.user.constants;

import lombok.Getter;

/**
 * 用户标签枚举
 *
 * @author Xinkang Wu
 * @date 2025/6/2 17:01
 */
@Getter
public enum UserTagsEnum {

    IS_RICH(1, "是否是富有用户", "tag_info_01"),
    IS_VIP((long) 1 << 1, "是否是VIP用户", "tag_info_01"),
    IS_OLD_USER((long) 1 << 2, "是否是老用户", "tag_info_01");

    final long tag;
    final String desc;
    final String fieldName;

    UserTagsEnum(long tag, String desc, String fieldName) {
        this.tag = tag;
        this.desc = desc;
        this.fieldName = fieldName;
    }
}
