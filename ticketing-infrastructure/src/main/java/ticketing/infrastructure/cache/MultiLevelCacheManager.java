package ticketing.infrastructure.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.Collection;

@Component
@Primary
public class MultiLevelCacheManager implements CacheManager {
    private final CacheManager caffeineCacheManager;
    private final CacheManager redisCacheManager;

    public MultiLevelCacheManager(
            @Qualifier("caffeineCacheManager")
            CacheManager caffeineCacheManager,
            @Qualifier("redisCacheManager")
            CacheManager redisCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
    }
    @Override
    public Cache getCache(String name) {
        Cache l1 = caffeineCacheManager != null ? caffeineCacheManager.getCache(name) : null;
        Cache l2 = redisCacheManager != null ? redisCacheManager.getCache(name) : null;

        if (l1 == null && l2 == null) {
            return null;
        }
        if (l1 == null) {
            return l2;
        }
        if (l2 == null) {
            return l1;
        }
        return new MultiLevelCache(l1, l2);
    }

    @Override
    public Collection<String> getCacheNames() {
        return redisCacheManager.getCacheNames();
    }
}
