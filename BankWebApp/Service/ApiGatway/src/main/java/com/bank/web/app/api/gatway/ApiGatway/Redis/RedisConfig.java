package com.bank.web.app.api.gatway.ApiGatway.Redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

//@Configuration
public class RedisConfig {
    private String test;
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // Key serializer
//        template.setKeySerializer(new StringRedisSerializer());
//
//        // Value serializer (JSON)
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.activateDefaultTyping(
//                BasicPolymorphicTypeValidator.builder().build(),
//                ObjectMapper.DefaultTyping.NON_FINAL
//        );
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
//
//        return template;
//    }
}
