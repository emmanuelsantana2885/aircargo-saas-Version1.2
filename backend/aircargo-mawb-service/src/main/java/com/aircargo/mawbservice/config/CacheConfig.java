package com.aircargo.mawbservice.config;

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
        manager.setCacheNames(Arrays.asList("mawbs", "hawbs", "dua-records"));
        manager.registerCustomCache("mawbs",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("hawbs",
                Caffeine.newBuilder()
                        .maximumSize(300)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("dua-records",
                Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        return manager;
    }
}