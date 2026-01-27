package com.zjxjwxk.live.api.controller;

import com.zjxjwxk.live.user.dto.UserDTO;
import com.zjxjwxk.live.user.interfaces.IUserRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Xinkang Wu
 * @date 2025/4/19 18:59
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @DubboReference
    private IUserRpc userRpc;

    @GetMapping("/getUserInfo")
    public UserDTO getUserInfo(Long userId) {
        UserDTO userDTO = userRpc.getByUserId(userId);
        log.info("getUserInfo return UserDTO: {}", userDTO);
        return userDTO;
    }

    @GetMapping("/batchQueryUserInfo")
    public Map<Long, UserDTO> batchQueryUserInfo(String userIdStr) {
        return userRpc.batchQueryUserInfo(Arrays.stream(userIdStr.split(",")).map(Long::valueOf).toList());
    }

    @PostMapping("/updateUserInfo")
    public boolean updateUserInfo(Long userId, String nickname) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickname);
        return userRpc.updateUserInfo(userDTO);
    }

    @PostMapping("/insertOne")
    public boolean insertOne(Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName("Test NickName");
        userDTO.setSex(1);
        return userRpc.insertOne(userDTO);
    }
}
