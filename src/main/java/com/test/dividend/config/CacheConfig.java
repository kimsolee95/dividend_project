package com.test.dividend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
public class CacheConfig {

  @Value("${spring.redis.host}")
  private String host;

  @Value("${spring.redis.port}")
  private int port;

  @Bean
  public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

    RedisCacheConfiguration conf = RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        //ttl을 설정해서 캐시 데이터 유효기간 설정 가능
    return RedisCacheManager.RedisCacheManagerBuilder
                          .fromConnectionFactory(redisConnectionFactory)
                          .cacheDefaults(conf)
                          .build();
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {

    RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
    conf.setHostName(host);
    conf.setPort(port);
    return new LettuceConnectionFactory(conf);
  }
}
