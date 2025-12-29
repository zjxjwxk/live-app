package com.zjxjwxk.live.user.provider.config;

import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *  RocketMQ生产者Bean配置类
 *
 * @author Xinkang Wu
 * @date 2025/4/26 13:38
 */
@Configuration
public class RocketMQProducerConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(RocketMQProducerConfig.class);
    @Resource
    private RocketMQProducerProperties rocketMQProducerProperties;
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public MQProducer mqProducer() {
        ThreadPoolExecutor asyncThreadPoolExecutor = new ThreadPoolExecutor(100, 150, 3, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setName(applicationName + ":rocketmq-producer:" + ThreadLocalRandom.current().nextInt(1000));
            return thread;
        });

        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        defaultMQProducer.setNamesrvAddr(rocketMQProducerProperties.getNameSrvAddr());
        defaultMQProducer.setProducerGroup(rocketMQProducerProperties.getGroupName());
        defaultMQProducer.setRetryTimesWhenSendFailed(rocketMQProducerProperties.getRetryTimes());
        defaultMQProducer.setRetryTimesWhenSendAsyncFailed(rocketMQProducerProperties.getRetryTimes());
        defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(true);
        // 设置异步发送线程池
        defaultMQProducer.setAsyncSenderExecutor(asyncThreadPoolExecutor);
        try {
            defaultMQProducer.start();
            LOGGER.info("MQ生产者启动成功，NameSrv is {}", rocketMQProducerProperties.getNameSrvAddr());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
        return defaultMQProducer;
    }
}
