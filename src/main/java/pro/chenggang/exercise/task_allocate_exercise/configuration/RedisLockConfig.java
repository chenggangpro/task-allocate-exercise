package pro.chenggang.exercise.task_allocate_exercise.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;
import pro.chenggang.exercise.task_allocate_exercise.properties.RedisLockProperties;
import pro.chenggang.exercise.task_allocate_exercise.redislock.RedisLockAspect;

import java.util.concurrent.TimeUnit;

/**
 * RedisLockConfig
 * @author chenggang
 * @date 2019/04/27
 */
@Slf4j
@Configuration
public class RedisLockConfig {

    @Bean
    @ConfigurationProperties(RedisLockProperties.REDIS_LOCK_PROPERTIES_PREFIX)
    public RedisLockProperties redisLockProperties(){
        return new RedisLockProperties();
    }

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory connectionFactory, RedisLockProperties redisLockProperties){
        String keys = redisLockProperties.getRegistryKeys();
        keys = StringUtils.isBlank(keys)?"LOCK_REDIS": keys;
        RedisLockRegistry redisLockRegistry = new RedisLockRegistry(connectionFactory, keys,TimeUnit.MILLISECONDS.convert(redisLockProperties.getExpireAfter(),redisLockProperties.getExpireAfterTimeUnit()));
        log.info("Load RedisLockRegistry Success,RegistryKeys:{}",keys);
        return redisLockRegistry;
    }

    @Bean
    public RedisLockAspect redisLockAspect(RedisLockRegistry redisLockRegistry){
        RedisLockAspect redisLockAspect = new RedisLockAspect(redisLockRegistry);
        log.info("Load RedisLockAspect Success");
        return redisLockAspect;
    }

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        redisTemplate.afterPropertiesSet();
        log.debug("Load Application RedisTemplate Success");
        return redisTemplate;
    }
}