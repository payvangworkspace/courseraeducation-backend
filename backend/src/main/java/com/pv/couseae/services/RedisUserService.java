package com.pv.couseae.services;

import com.pv.couseae.Dtos.UserCacheDTO;
import com.pv.couseae.utill.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisUserService {

    private final RedisTemplate<String, UserCacheDTO> redisTemplate;
    private final AuthUtils authUtils;
    /**
     * Get user only from Redis (no DB fallback).
     */
    public UserCacheDTO getUserFromRedis(String username) {
        String redisKey = authUtils.getRedisUserKey(username);
        UserCacheDTO cachedUser = redisTemplate.opsForValue().get(redisKey);

        if (cachedUser == null) {
            log.warn("cachedUser {} not found in Redis", username);
            return null;
        }else if (cachedUser instanceof UserCacheDTO) {
            return cachedUser;
        }

        log.error("Invalid object type found in Redis for key {}", redisKey);
        return null;
    }
    /**
     * Get user only from Redis (no DB fallback).
     */


}