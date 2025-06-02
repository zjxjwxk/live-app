package com.zjxjwxk.live.id.generate.service.bo;

import lombok.*;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 本地无序id段BO
 *
 * @author Xinkang Wu
 * @date 2025/5/31 14:49
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocalUnSeqIdBO {

    /**
     * id生成策略
     */
    private int id;

    /**
     * 提前将无序id存入队列中
     */
    private ConcurrentLinkedQueue<Long> idQueue;

    /**
     * 当前id段的开始值
     */
    private Long currentStart;

    /**
     * 当前id段的阈值
     */
    private Long nextThreshold;
}
