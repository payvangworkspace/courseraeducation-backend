package com.pv.couseae.services;

import com.pv.couseae.model.MerchantModel;
import com.pv.couseae.model.UserListModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MerchantRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Inject the object template explicitly so there is no ambiguity about which serializer is used.
    public MerchantRedisService(@Qualifier("objectRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ---- MerchantModel ----

    public void saveMerchantsMerchantModel(String key, List<MerchantModel> merchants) {
        redisTemplate.opsForValue().set(key, merchants);
    }

    @SuppressWarnings("unchecked")
    public List<MerchantModel> getMerchantsMerchantModel(String key) {
        try {
            return (List<MerchantModel>) redisTemplate.opsForValue().get(key);
        } catch (SerializationException e) {
            log.warn("Corrupt/incompatible cache at key '{}' — evicting and returning null (caller reloads from DB)", key, e);
            safeDelete(key);
            return null;
        }
    }

    // ---- UserListModel ----

    public void saveMerchantsUserListModel(String key, List<UserListModel> merchants) {
        redisTemplate.opsForValue().set(key, merchants);
    }

    @SuppressWarnings("unchecked")
    public List<UserListModel> getMerchantsUserListModel(String key) {
        try {
            return (List<UserListModel>) redisTemplate.opsForValue().get(key);
        } catch (SerializationException e) {
            log.warn("Corrupt/incompatible cache at key '{}' — evicting and returning null (caller reloads from DB)", key, e);
            safeDelete(key);
            return null;
        }
    }

    // ---- Delete ----

    public Boolean deleteMerchants(String key) {
        return redisTemplate.delete(key);
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.error("Failed to evict corrupt cache key '{}'", key, ex);
        }
    }
}