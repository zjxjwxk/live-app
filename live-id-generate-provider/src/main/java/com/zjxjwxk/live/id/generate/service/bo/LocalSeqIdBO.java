package com.zjxjwxk.live.id.generate.service.bo;

import lombok.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地有序id段BO
 *
 * @author Xinkang Wu
 * @date 2025/4/28 22:59
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocalSeqIdBO {

    /**
     * id生成策略
     */
    private int id;

    /**
     * 内存中记录的当前有序id的值
     */
    private AtomicLong currentNum;

    /**
     * 当前id段的开始值
     */
    private Long currentStart;

    /**
     * 当前id段的阈值
     */
    private Long nextThreshold;
}
