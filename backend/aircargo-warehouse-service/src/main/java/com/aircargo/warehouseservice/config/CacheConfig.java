package com.aircargo.warehouseservice.config;

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
        manager.setCacheNames(Arrays.asList("warehouse-receipts", "receipts", "receipt-pieces", "supporting-docs"));
        manager.registerCustomCache("warehouse-receipts",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("receipts",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("receipt-pieces",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());
        manager.registerCustomCache("supporting-docs",
                Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .build());
        return manager;
    }
}