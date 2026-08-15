package com.aircargo.common.cache;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caché Caffeine compartida por todos los servicios (antes 9 clases CacheConfig duplicadas).
 *
 * La spec se lee de {@code spring.cache.caffeine.spec} (env: SPRING_CACHE_CAFFEINE_SPEC), lo que
 * permite ajustar tamaño/TTL sin recompilar. Para DESACTIVAR la caché (p. ej. en despliegue con
 * múltiples réplicas sin Redis) basta con {@code spring.cache.type=none}.
 *
 * No se registra en el gateway (reactivo, sin caffeine): {@link ConditionalOnClass}.
 */
@Configuration
@EnableCaching
@ConditionalOnClass(name = "org.springframework.cache.caffeine.CaffeineCacheManager")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value("${spring.cache.caffeine.spec:maximumSize=500,expireAfterWrite=300s}") String caffeineSpec) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeineSpec(CaffeineSpec.parse(caffeineSpec));
        return manager;
    }
}
