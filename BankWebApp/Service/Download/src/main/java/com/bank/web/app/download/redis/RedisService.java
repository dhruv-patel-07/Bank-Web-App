package com.bank.web.app.download.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private static final int MAX_REQUESTS_PER_DAY = 4;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    public boolean isAllowed(String userId) {
        String key = "rate_limit:" + userId;
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount == null) {
            // Handle Redis issue, allow or deny based on policy
            return true;
        }
        if (currentCount == 1) {
            // Set 24h expiry for this key
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        return currentCount <= MAX_REQUESTS_PER_DAY;
    }


//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    public void setValueWithExpire(String value) {
//        ValueOperations<String, String> ops = redisTemplate.opsForValue();
//        ops.set("service-account", value, Duration.ofMinutes(10)); // 10-minute TTL
//    }

//    public String getValue() {
//        ValueOperations<String, String> ops = redisTemplate.opsForValue();
//        return ops.get("service-account");
//    }
//
//    public boolean keyExists() {
//        return Boolean.TRUE.equals(redisTemplate.hasKey("service-account"));
//    }

}
