package com.aircargo.uldservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(java.util.Arrays.asList("ulds", "uld-awbs"));
        manager.registerCustomCache("ulds",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("uld-awbs",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        return manager;
    }
}
