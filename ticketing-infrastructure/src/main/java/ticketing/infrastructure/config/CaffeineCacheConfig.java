package ticketing.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager =
                new CaffeineCacheManager();

        manager.registerCustomCache(
                "tickets",
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(
                                10,
                                TimeUnit.MINUTES
                        )
                        .recordStats()
                        .build()
        );

        return manager;
    }
}
