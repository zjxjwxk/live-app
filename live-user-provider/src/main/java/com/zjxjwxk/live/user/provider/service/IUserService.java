package com.zjxjwxk.live.user.provider.service;

import com.zjxjwxk.live.user.dto.UserDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Xinkang Wu
 * @date 2025/4/16 22:28
 */
public interface IUserService {

    /**
     * 根据userId查询用户信息
     */
    UserDTO getByUserId(Long userId);

    /**
     * 更新用户信息
     *
     */
    boolean updateUserInfo(UserDTO userDTO);

    /**
     * 插入用户信息
     *
     */
    boolean insertOne(UserDTO userDTO);

    /**
     * 批量根据userId查询用户信息
     *
     */
    Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList);
}
