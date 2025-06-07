package com.zjxjwxk.live.user.provider.config;

import com.alibaba.fastjson2.JSON;
import com.zjxjwxk.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import com.zjxjwxk.live.user.dto.UserCacheDeleteAsyncDTO;
import com.zjxjwxk.live.user.constants.UserCacheDeleteAsyncCode;
import com.zjxjwxk.live.user.provider.constants.RocketMQTopic;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * RocketMQ消费者Bean配置类
 *
 * @author Xinkang Wu
 * @date 2025/4/26 14:03
 */
@Configuration
public class RocketMQConsumerConfig implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Override
    public void afterPropertiesSet() {
        initConsumer();
    }

    private void initConsumer() {
        try {
            DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
            defaultMQPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrvAddr());
            defaultMQPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName());
            defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            defaultMQPushConsumer.subscribe(RocketMQTopic.DELETE_USER_CACHE_ASYNC, "*");

            defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                String json = new String(msgs.get(0).getBody());
                UserCacheDeleteAsyncDTO userCacheDeleteAsyncDTO = JSON.parseObject(json, UserCacheDeleteAsyncDTO.class);

                // 延迟删除用户信息
                if (UserCacheDeleteAsyncCode.USER_INFO.getCode() == userCacheDeleteAsyncDTO.getCode()) {
                    Long userId = JSON.parseObject(userCacheDeleteAsyncDTO.getJson()).getLong("userId");
                    if (userId == null) {
                        LOGGER.error("MQ消费者：收到的消息体中UserId为空，参数异常，消息内容：{}", json);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    redisTemplate.delete(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
                    LOGGER.info("延迟删除用户信息缓存完成，userId is {}", userId);
                } else if (UserCacheDeleteAsyncCode.USER_TAG.getCode() == userCacheDeleteAsyncDTO.getCode()) {
                    // 延迟删除用户标签
                    Long userId = JSON.parseObject(userCacheDeleteAsyncDTO.getJson()).getLong("userId");
                    if (userId == null) {
                        LOGGER.error("MQ消费者：收到的消息体中UserId为空，参数异常，消息内容：{}", json);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    redisTemplate.delete(userProviderCacheKeyBuilder.buildTagInfoKey(userId));
                    LOGGER.info("延迟删除用户标签缓存完成，userId is {}", userId);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            defaultMQPushConsumer.start();
            LOGGER.info("MQ消费者启动成功，NameSrv is {}", rocketMQConsumerProperties.getNameSrvAddr());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }
}
