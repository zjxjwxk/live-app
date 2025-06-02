package com.zjxjwxk.live.id.generate.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author Xinkang Wu
 * @date 2025/4/28 23:15
 */
@TableName("t_id_generate_config")
@Getter
@Setter
@ToString
public class IdGeneratePO {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * id备注描述
     */
    private String remark;

    /**
     * 初始化值
     */
    private long initNum;
    /**
     * 步长
     */
    private int step;

    /**
     * 是否是有序的id
     */
    private int isSeq;

    /**
     * 当前id段的开始值
     */
    private long currentStart;

    /**
     * 当前id段的阈值
     */
    private long nextThreshold;

    /**
     * 业务代码前缀
     */
    private String idPrefix;

    /**
     * 乐观锁版本号
     */
    private int version;

    private Date createTime;

    private Date updateTime;
}
