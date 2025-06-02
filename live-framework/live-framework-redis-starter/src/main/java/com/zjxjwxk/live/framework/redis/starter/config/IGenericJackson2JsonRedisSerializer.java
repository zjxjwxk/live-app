package com.zjxjwxk.live.framework.redis.starter.config;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author Xinkang Wu
 * @date 2025/4/20 18:41
 */
public class IGenericJackson2JsonRedisSerializer extends
        GenericJackson2JsonRedisSerializer {

    public IGenericJackson2JsonRedisSerializer() {
        super(MapperFactory.newInstance());
    }

    @Override
    public byte[] serialize(Object source) throws SerializationException {
        if (source != null && ((source instanceof String) ||
                (source instanceof Character))) {
            return source.toString().getBytes();
        }
        return super.serialize(source);
    }
}