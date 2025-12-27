package com.zjxjwxk.live.framework.redis.starter.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author Xinkang Wu
 * @date 2025/4/21 19:36
 */
public class RedisKeyLoadMatch implements Condition {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisKeyLoadMatch.class);
    private static final String PREFIX = "live";

    @Override
    public boolean matches(ConditionContext context,
                           AnnotatedTypeMetadata metadata) {
        String appName = context.getEnvironment().getProperty("spring.application.name");
        if (appName == null) {
            LOGGER.error("没有匹配到应用名称，所以无法加载任何RedisKeyBuilder对象");
            return false;
        }
        try {
            // 获取当前 CacheKeyBuilder 类名称
            Field classNameField = metadata.getClass().getDeclaredField("className");
            classNameField.setAccessible(true);
            String keyBuilderClassFullName = (String) classNameField.get(metadata);
            boolean matchStatus = isMatchStatus(keyBuilderClassFullName, appName);

            LOGGER.info("keyBuilderClass is {}, matchStatus is {}", keyBuilderClassFullName, matchStatus);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static boolean isMatchStatus(String keyBuilderClassFullName, String appName) {
        List<String> splitList = Arrays.asList(keyBuilderClassFullName.split("\\."));
        String keyBuilderClassName = splitList.get(splitList.size() - 1).toLowerCase();

        // 统一用 live 开头命名，如 live + UserProviderCacheKeyBuilder
        String keyBuilderClassNameWithPrefix = PREFIX + keyBuilderClassName;

        // 判断当前 CacheKeyBuilder 名称是否包含当前应用名称，如 liveUserProviderCacheKeyBuilder 包含 liveUserProvider
        return keyBuilderClassNameWithPrefix.contains(appName.replaceAll("-", ""));
    }
}
