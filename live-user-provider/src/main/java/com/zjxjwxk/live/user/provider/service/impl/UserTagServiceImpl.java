package com.zjxjwxk.live.user.provider.service.impl;

import com.google.common.base.CaseFormat;
import com.zjxjwxk.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import com.zjxjwxk.live.user.constants.UserTagsEnum;
import com.zjxjwxk.live.user.provider.dao.mapper.IUserTagMapper;
import com.zjxjwxk.live.user.provider.dao.po.UserTagPO;
import com.zjxjwxk.live.user.provider.service.IUserTagService;
import com.zjxjwxk.live.user.utils.UserTagUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

/**
 * @author Xinkang Wu
 * @date 2025/6/2 17:04
 */
@Service
public class UserTagServiceImpl implements IUserTagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTagServiceImpl.class);

    @Resource
    private IUserTagMapper userTagMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (updateStatus) {
            return true;
        }

        // 处理设置标签失败

        // 1. 获得Redis分布式锁
        String setNxKey = cacheKeyBuilder.buildTagLockKey(userId);
        String setNxResult = redisTemplate.execute((RedisCallback<String>) connection -> {
            RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
            RedisSerializer<String> valueSerializer = (RedisSerializer<String>) redisTemplate.getValueSerializer();
            return (String) connection.execute("set",
                    keySerializer.serialize(setNxKey),
                    valueSerializer.serialize("-1"),
                    "NX".getBytes(StandardCharsets.UTF_8),
                    "EX".getBytes(StandardCharsets.UTF_8),
                    "10".getBytes(StandardCharsets.UTF_8));
        });
        if (!"OK".equals(setNxResult)) {
            return false;
        }

        // 2. 失败场景一：已经存在该标签记录，则表示该标签已设置
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO != null) {
            return false;
        }

        // 3. 失败场景二：不存在该标签记录，则插入标签记录并设置该标签
        userTagPO = UserTagPO.builder().userId(userId).build();
        userTagMapper.insert(userTagPO);
        updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        LOGGER.info("插入用户标签记录，并设置标签成功");

        // 释放Redis分布式锁
        redisTemplate.delete(setNxKey);

        return updateStatus;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null) {
            return false;
        }
        String fieldName = userTagsEnum.getFieldName();
        String methodName = "get" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, fieldName);
        try {
            Long tagInfo = (Long) UserTagPO.class.getMethod(methodName).invoke(userTagPO);
            return UserTagUtils.isContain(tagInfo, userTagsEnum.getTag());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
