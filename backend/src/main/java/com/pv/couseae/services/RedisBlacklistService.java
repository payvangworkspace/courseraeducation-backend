package com.pv.couseae.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisBlacklistService {
    private final StringRedisTemplate redisTemplate;

    public RedisBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String token, long expiry) {
        long ttlSeconds = Math.max(1, (expiry - System.currentTimeMillis()) / 1000);
        String key = "jwt:blacklist:" + token;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String token) {
        String key = "jwt:blacklist:" + token;
        Boolean has = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(has);
    }
}
