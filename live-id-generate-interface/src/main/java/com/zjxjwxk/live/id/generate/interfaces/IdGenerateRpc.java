package com.zjxjwxk.live.id.generate.interfaces;

/**
 * @author Xinkang Wu
 * @date 2025/4/28 22:29
 */
public interface IdGenerateRpc {

    /**
     * 获取有序id
     */
    Long getSeqId(Integer id);

    /**
     * 获取无序id
     */
    Long getUnSeqId(Integer id);
}
