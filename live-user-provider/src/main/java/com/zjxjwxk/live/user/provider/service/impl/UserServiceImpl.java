package com.zjxjwxk.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.zjxjwxk.live.common.interfaces.ConvertBeanUtils;
import com.zjxjwxk.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import com.zjxjwxk.live.user.constants.UserCacheDeleteAsyncCode;
import com.zjxjwxk.live.user.dto.UserCacheDeleteAsyncDTO;
import com.zjxjwxk.live.user.dto.UserDTO;
import com.zjxjwxk.live.user.provider.constants.RocketMQTopic;
import com.zjxjwxk.live.user.provider.dao.mapper.IUserMapper;
import com.zjxjwxk.live.user.provider.dao.po.UserPO;
import com.zjxjwxk.live.user.provider.service.IUserService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Xinkang Wu
 * @date 2025/4/16 22:29
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;
    @Resource
    private RedisTemplate<String, UserDTO> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;
    @Resource
    private MQProducer mqProducer;

    @Override
    public UserDTO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        String redisKey = userProviderCacheKeyBuilder.buildUserInfoKey(userId);

        // 查询Redis缓存
        UserDTO userDTO = redisTemplate.opsForValue().get(redisKey);
        if (userDTO != null) {
            return userDTO;
        }

        // 查询DB
        userDTO = ConvertBeanUtils.convert(userMapper.selectById(userId), UserDTO.class);
        if (userDTO != null) {
            // 加入Redis缓存，过期时间30分钟
            redisTemplate.opsForValue().set(redisKey, userDTO, 30, TimeUnit.MINUTES);
        }
        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class));

        // 更新DB后，删除Redis缓存
        String redisKey = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
        redisTemplate.delete(redisKey);

        // 利用RocketMQ作延迟双删
        Map<String, Object> jsonParam = new HashMap<>();
        jsonParam.put("userId", userDTO.getUserId());

        UserCacheDeleteAsyncDTO userCacheDeleteAsyncDTO = new UserCacheDeleteAsyncDTO();
        userCacheDeleteAsyncDTO.setCode(UserCacheDeleteAsyncCode.USER_INFO.getCode());
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
        return true;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        userMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class));
        return true;
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return new HashMap<>();
        }
        // 过滤掉userId <= 10000的数据
        userIdList = userIdList.stream().filter(userId -> userId > 10000).toList();
        if (CollectionUtils.isEmpty(userIdList)) {
            return new HashMap<>();
        }

        // 批量查询Redis缓存
        List<String> cacheKeyList = userIdList.stream().map(userId -> userProviderCacheKeyBuilder.buildUserInfoKey(userId)).toList();
        List<UserDTO> userDTOList = redisTemplate.opsForValue().multiGet(cacheKeyList);

        // 过滤掉为null的对象
        if (!CollectionUtils.isEmpty(userDTOList)) {
            userDTOList = userDTOList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            userDTOList = new ArrayList<>();
        }

        // 若全部击中缓存，则直接返回
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, userDTO -> userDTO));
        }

        if (!CollectionUtils.isEmpty(userDTOList)) {
            // 过滤掉已经从Redis缓存中查询到的UserId
            List<Long> userIdInCacheList = userDTOList.stream().map(UserDTO::getUserId).toList();
            userIdList = userIdList.stream().filter(userId -> !userIdInCacheList.contains(userId)).toList();
        }

        // 未击中缓存部分查询DB，根据分表规则对UserId分组，并利用多线程对各个分表进行批量查询和归并（而不是直接对DB作Union All操作）
        Map<Long, List<Long>> userIdMap = userIdList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryList = new CopyOnWriteArrayList<>();
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            List<UserPO> userList = userMapper.selectBatchIds(queryUserIdList);
            dbQueryList.addAll(ConvertBeanUtils.convertList(userList, UserDTO.class));
        });

        if (!CollectionUtils.isEmpty(dbQueryList)) {
            // 将未击中缓存部分批量存入缓存，减少网络IO开销
            Map<String, UserDTO> saveCacheMap = dbQueryList.stream().collect(Collectors.toMap(userDTO -> userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()), userDTO -> userDTO));
            redisTemplate.opsForValue().multiSet(saveCacheMap);

            // 利用Pipeline批量对每个key设置30~60分钟的随机过期时间，减少缓存雪崩现象
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, randomExpireTime(), TimeUnit.MINUTES);
                    }
                    return null;
                }
            });

            // 将DB查询结果合并
            userDTOList.addAll(dbQueryList);
        }
        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, userDTO -> userDTO));
    }

    private int randomExpireTime() {
        int randomTime = ThreadLocalRandom.current().nextInt(30);
        return 30 + randomTime;
    }
}
