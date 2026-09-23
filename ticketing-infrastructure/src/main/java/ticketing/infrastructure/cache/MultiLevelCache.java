package ticketing.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Component
@Slf4j
public class MultiLevelCache implements Cache {
    private final Cache l1;
    private final Cache l2;
    @Override
    public String getName() {
        return l1 != null ? l1.getName() : (l2 != null ? l2.getName() : "multi-level-cache");
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper l1Value = l1 != null ? l1.get(key) : null;
        if (l1Value != null) {
            log.debug("L1 cache hit for key={}", key);
            return l1Value;
        }

        ValueWrapper l2Value = l2 != null ? l2.get(key) : null;
        if (l2Value != null) {
            Object value = l2Value.get();
            if (l1 != null) {
                l1.put(key, value);
                log.debug("Promoted key={} from L2 to L1", key);
            }
            return l2Value;
        }
        log.debug("Cache miss for key={}", key);
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = l1 != null ? l1.get(key, type) : null;
        if (value != null) {
            log.debug("L1 cache typed hit for key={}", key);
            return value;
        }

        value = l2 != null ? l2.get(key, type) : null;
        if (value != null) {
            if (l1 != null) l1.put(key, value);
            log.debug("Promoted typed key={} from L2 to L1", key);
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // Try L1
        ValueWrapper l1Value = l1 != null ? l1.get(key) : null;
        if (l1Value != null) {
            @SuppressWarnings("unchecked")
            T v = (T) l1Value.get();
            return v;
        }

        // Try L2 and promote
        ValueWrapper l2Value = l2 != null ? l2.get(key) : null;
        if (l2Value != null) {
            @SuppressWarnings("unchecked")
            T v = (T) l2Value.get();
            if (l1 != null) l1.put(key, v);
            return v;
        }

        // Load and populate both
        try {
            T loaded = valueLoader.call();
            if (loaded != null) {
                if (l1 != null) l1.put(key, loaded);
                if (l2 != null) l2.put(key, loaded);
            }
            return loaded;
        } catch (Exception ex) {
            throw new Cache.ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        // Check L1 first
        ValueWrapper existing = l1 != null ? l1.get(key) : null;
        if (existing != null) {
            return existing;
        }

        // Not in L1: attempt to putIfAbsent in L2 if supported, otherwise put
        ValueWrapper existingL2 = l2 != null ? l2.get(key) : null;
        if (existingL2 != null) {
            // promote to L1
            Object v = existingL2.get();
            if (l1 != null) l1.put(key, v);
            return existingL2;
        }

        // Absent in both: put to both
        if (l1 != null) l1.put(key, value);
        if (l2 != null) {
            try {
                // try to use putIfAbsent if underlying cache supports it
                l2.putIfAbsent(key, value);
            } catch (UnsupportedOperationException e) {
                l2.put(key, value);
            }
        }
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        if (l1 != null) l1.put(key, value);
        if (l2 != null) l2.put(key, value);
    }

    @Override
    public void evict(Object key) {
        if (l1 != null) l1.evict(key);
        if (l2 != null) l2.evict(key);
    }

    @Override
    public void clear() {
        if (l1 != null) l1.clear();
        if (l2 != null) l2.clear();
    }
}
