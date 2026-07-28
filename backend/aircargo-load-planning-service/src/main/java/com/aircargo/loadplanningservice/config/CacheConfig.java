package com.aircargo.loadplanningservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(Arrays.asList("load-plans", "uld-awbs"));
        manager.registerCustomCache("load-plans",
                Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("uld-awbs",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(3, TimeUnit.MINUTES)
                        .build());
        return manager;
    }
}
