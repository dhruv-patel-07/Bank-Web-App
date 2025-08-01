package com.bank.web.schedule.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void setValueWithExpire(String value) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("service-account", value, Duration.ofMinutes(10)); // 10-minute TTL
    }

    public String getValue() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        return ops.get("service-account");
    }

    public boolean keyExists() {
        return Boolean.TRUE.equals(redisTemplate.hasKey("service-account"));
    }

}
