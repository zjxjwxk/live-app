package com.zjxjwxk.live.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Xinkang Wu
 * @date 2025/6/7 14:03
 */
@Getter
@Setter
public class UserTagDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -605706576360153186L;

    private Long userId;
    private Long tagInfo01;
    private Long tagInfo02;
    private Long tagInfo03;
    private Date createTime;
    private Date updateTime;
}
