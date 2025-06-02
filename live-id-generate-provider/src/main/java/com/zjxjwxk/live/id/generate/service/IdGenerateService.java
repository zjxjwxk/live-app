package com.zjxjwxk.live.id.generate.service;

/**
 * @author Xinkang Wu
 * @date 2025/4/28 22:38
 */
public interface IdGenerateService {

    /**
     * 获取有序id
     */
    Long getSeqId(Integer id);

    /**
     * 获取无序id
     */
    Long getUnSeqId(Integer id);
}
