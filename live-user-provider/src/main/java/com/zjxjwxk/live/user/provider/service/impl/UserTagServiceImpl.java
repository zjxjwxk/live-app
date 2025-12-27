package com.zjxjwxk.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.CaseFormat;
import com.zjxjwxk.live.common.interfaces.utils.ConvertBeanUtils;
import com.zjxjwxk.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import com.zjxjwxk.live.user.constants.UserCacheDeleteAsyncCode;
import com.zjxjwxk.live.user.constants.UserTagsEnum;
import com.zjxjwxk.live.user.dto.UserCacheDeleteAsyncDTO;
import com.zjxjwxk.live.user.dto.UserTagDTO;
import com.zjxjwxk.live.user.provider.constants.RocketMQTopic;
import com.zjxjwxk.live.user.provider.dao.mapper.IUserTagMapper;
import com.zjxjwxk.live.user.provider.dao.po.UserTagPO;
import com.zjxjwxk.live.user.provider.service.IUserTagService;
import com.zjxjwxk.live.user.utils.UserTagUtils;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplate<String, UserTagDTO> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private MQProducer mqProducer;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (updateStatus) {
            // 设置标签成功，则删除相应Redis缓存
            deleteTagInfoFromRedis(userId);
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

        // 2. 失败场景一：已经存在该标签记录，则表示该标签已设置，直接返回
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
        boolean cancelStatus = userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (!cancelStatus) {
            return false;
        }
        // 删除标签成功，则删除相应Redis缓存
        deleteTagInfoFromRedis(userId);
        return true;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagDTO userTagDTO = queryTagInfoByUserIdFromRedis(userId);
        if (userTagDTO == null) {
            return false;
        }
        String fieldName = userTagsEnum.getFieldName();
        String methodName = "get" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, fieldName);
        try {
            Long tagInfo = (Long) UserTagDTO.class.getMethod(methodName).invoke(userTagDTO);
            return UserTagUtils.isContain(tagInfo, userTagsEnum.getTag());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从Redis缓存删除用户标签信息
     */
    private void deleteTagInfoFromRedis(Long userId) {
        String tagInfoKey = cacheKeyBuilder.buildTagInfoKey(userId);
        redisTemplate.delete(tagInfoKey);

        // 利用RocketMQ作延迟双删
        Map<String, Object> jsonParam = new HashMap<>();
        jsonParam.put("userId", userId);

        UserCacheDeleteAsyncDTO userCacheDeleteAsyncDTO = new UserCacheDeleteAsyncDTO();
        userCacheDeleteAsyncDTO.setCode(UserCacheDeleteAsyncCode.USER_TAG.getCode());
        userCacheDeleteAsyncDTO.setJson(JSON.toJSONString(jsonParam));

        Message message = new Message();
        message.setTopic(RocketMQTopic.DELETE_USER_CACHE_ASYNC);
        message.setBody(JSON.toJSONString(userCacheDeleteAsyncDTO).getBytes());
        // 延迟1秒发送
        message.setDelayTimeLevel(1);
        try {
            mqProducer.send(message);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 先从Redis缓存查询用户标签信息，
     * 若不存在则从数据库查询，并写入Redis缓存
     */
    private UserTagDTO queryTagInfoByUserIdFromRedis(Long userId) {
        // 查询Redis缓存
        String tagInfoKey = cacheKeyBuilder.buildTagInfoKey(userId);
        UserTagDTO userTagDTO = redisTemplate.opsForValue().get(tagInfoKey);
        if (userTagDTO != null) {
            return userTagDTO;
        }
        // 查询DB
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null) {
            return null;
        }
        userTagDTO = ConvertBeanUtils.convert(userTagPO, UserTagDTO.class);
        // 查询结果写入Redis缓存
        redisTemplate.opsForValue().set(tagInfoKey, userTagDTO);
        redisTemplate.expire(tagInfoKey, 30, TimeUnit.MINUTES);
        return userTagDTO;
    }
}
