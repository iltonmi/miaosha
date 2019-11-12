package com.linweili.miaosha.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.linweili.miaosha.serializer.JodaTimeFastJsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {


    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //首先解决解决key得序列化方式
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);

        //解决value得序列化方式
        ParserConfig.getGlobalInstance().addAccept("com.linweili.miaosha.service.model.");
        redisTemplate.setValueSerializer(new JodaTimeFastJsonSerializer<>(Object.class));

        return redisTemplate;
    }

}
