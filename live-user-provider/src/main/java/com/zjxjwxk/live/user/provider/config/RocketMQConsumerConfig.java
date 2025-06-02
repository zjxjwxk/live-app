package com.zjxjwxk.live.user.provider.config;

import com.alibaba.fastjson2.JSON;
import com.zjxjwxk.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import com.zjxjwxk.live.user.dto.UserDTO;
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
            defaultMQPushConsumer.subscribe(RocketMQTopic.USER_UPDATE_CACHE, "*");

            defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                String msgStr = new String(msgs.get(0).getBody());
                UserDTO userDTO = JSON.parseObject(msgStr, UserDTO.class);

                if (userDTO == null || userDTO.getUserId() == null) {
                    LOGGER.error("MQ消费者：接受消息中UserId为空，参数异常，消息内容：{}", msgStr);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                String redisKey = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
                redisTemplate.delete(redisKey);
                LOGGER.info("延迟删除处理完成，userDTO is {}", userDTO);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            defaultMQPushConsumer.start();
            LOGGER.info("MQ消费者启动成功，NameSrv is {}", rocketMQConsumerProperties.getNameSrvAddr());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }
}
