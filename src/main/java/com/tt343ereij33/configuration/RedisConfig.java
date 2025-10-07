package com.tt343ereij33.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {
    @Bean(destroyMethod = "close")
    public JedisPooled jedisPool() {
        return new JedisPooled(
                System.getenv("REDIS_HOST"),
                Integer.parseInt(System.getenv("REDIS_PORT")),
                System.getenv("REDIS_USER"),
                System.getenv("REDIS_PASSWORD")
        );
    }
}
