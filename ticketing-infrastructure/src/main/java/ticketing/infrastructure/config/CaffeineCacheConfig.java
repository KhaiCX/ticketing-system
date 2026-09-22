package ticketing.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    @Bean
    public CaffeineCache caffeineCache() {
        return new CaffeineCache("tickets", Caffeine.newBuilder()
                .initialCapacity(1)
                .maximumSize(2000)
                .expireAfterWrite(
                        10,
                        TimeUnit.MINUTES
                )
                .recordStats()
                .build());
    }

    @Primary
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager(CaffeineCache caffeineCache) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Collections.singletonList(caffeineCache));
        return manager;
    }
}
