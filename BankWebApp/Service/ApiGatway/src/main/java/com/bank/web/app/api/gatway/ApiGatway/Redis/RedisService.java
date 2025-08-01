package com.bank.web.app.api.gatway.ApiGatway.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class RedisService {
//
//    private final ReactiveStringRedisTemplate redisTemplate;
//
//    public RedisService(ReactiveStringRedisTemplate redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    public Mono<Boolean> saveRefreshToken(String userId, String refreshToken, long ttlSeconds) {
//        return redisTemplate
//                .opsForValue()
//                .set("refresh:" + userId, refreshToken, Duration.ofSeconds(ttlSeconds));
//    }
//    public Mono<String> getRefreshToken(String key) {
//        return redisTemplate.opsForValue().get(key);
//    }
private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveHashOperations<String, String, String> hashOps;

    @Autowired
    public RedisService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // Save multiple fields in a Redis Hash
    public Mono<Boolean> saveRefreshToken(String userId, Map<String, String> tokenData, long ttlSeconds) {
        String redisKey = "refresh:" + userId;
        return redisTemplate.delete(redisKey)
                .then(hashOps.putAll(redisKey, tokenData))
                .then(redisTemplate.expire(redisKey, Duration.ofSeconds(ttlSeconds)));
    }

    // Get one field from hash (e.g., refresh_token)
    public Mono<String> getField(String userId, String field) {
        return hashOps.get("refresh:" + userId, field);
    }

    // Get all fields for a user
    public Mono<Map<String, String>> getAllFields(String userId) {
        return hashOps.entries("refresh:" + userId).collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

}
