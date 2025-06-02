package com.zjxjwxk.live.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Xinkang Wu
 * @date 2025/4/18 17:01
 */
@Getter
@Setter
@ToString
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3197480350459278071L;

    private Long userId;
    private String nickName;
    private String trueName;
    private String avatar;
    private Integer sex;
    private Integer workCity;
    private Integer bornCity;
    private Date bornDate;
    private Date createTime;
    private Date updateTime;
}
