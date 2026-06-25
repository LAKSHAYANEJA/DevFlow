package com.devflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.devflow.dto.ProjectResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // mapper.activateDefaultTyping(
        //     mapper.getPolymorphicTypeValidator(),
        //     ObjectMapper.DefaultTyping.NON_FINAL
        // );

        // GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(mapper);

        // GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(mapper);

        Jackson2JsonRedisSerializer<List<ProjectResponse.Summary>> listSerializer = new Jackson2JsonRedisSerializer<>(
            mapper, mapper.getTypeFactory().constructCollectionType(List.class, ProjectResponse.Summary.class)    
        );

        RedisCacheConfiguration listConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5)).
        serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(listSerializer)).disableCachingNullValues();

        // Default config for single-object caches (project, task)

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)).
        serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())).disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(defaultConfig).withInitialCacheConfigurations(Map.of("projects", listConfig)).build();
    }
}
