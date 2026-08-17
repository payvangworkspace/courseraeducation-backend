package com.pv.couseae.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pv.couseae.Dtos.UserCacheDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // ─────────────────────────────────────────────────────
    // Jackson ObjectMappers
    // ─────────────────────────────────────────────────────

    /**
     * Plain mapper — the one UserServiceImpl asks for by @Qualifier("plainObjectMapper").
     * NO default typing. Used for normal DTO/JSON conversion.
     * @Primary so Spring Boot's own MVC/auto-config usage picks this one.
     */
    @Bean(name = "plainObjectMapper")
    @Primary
    public ObjectMapper plainObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Typed mapper — PRIVATE method, never exposed as a bean.
     * Default typing ON so polymorphic lists round-trip correctly.
     * Only feeds the objectRedisTemplate serializer below.
     *
     * DO NOT turn this into a @Bean — if it leaks as a general ObjectMapper it will
     * poison every normal @RequestBody/@ResponseBody with type-id wrappers.
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.pv.couseae.")   // ← couseae package, not zenithpay
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    /** Plain mapper instance for the UserCacheDTO-bound serializer (no typing). */
    private ObjectMapper dtoObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // ─────────────────────────────────────────────────────
    // Redis Templates — three, each with ONE job
    // ─────────────────────────────────────────────────────

    @Bean(name = "objectRedisTemplate")
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer keySer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valSer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(keySer);
        template.setHashKeySerializer(keySer);
        template.setValueSerializer(valSer);
        template.setHashValueSerializer(valSer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "appStringRedisTemplate")   // ← renamed from "stringRedisTemplate"
    public RedisTemplate<String, String> appStringRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer ser = new StringRedisSerializer();
        template.setKeySerializer(ser);
        template.setHashKeySerializer(ser);
        template.setValueSerializer(ser);
        template.setHashValueSerializer(ser);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "userCacheRedisTemplate")
    public RedisTemplate<String, UserCacheDTO> userCacheRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, UserCacheDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer keySer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<UserCacheDTO> valSer =
                new Jackson2JsonRedisSerializer<>(dtoObjectMapper(), UserCacheDTO.class);

        template.setKeySerializer(keySer);
        template.setHashKeySerializer(keySer);
        template.setValueSerializer(valSer);
        template.setHashValueSerializer(valSer);

        template.afterPropertiesSet();
        return template;
    }
}