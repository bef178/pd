package pd.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TimedCache<T> implements AutoCloseable {

    private Map<String, TimedValue<T>> cache;

    private Timer purgeTimer;

    private boolean closed = false;

    public TimedCache() {
        this(Duration.ofSeconds(60), Duration.ofSeconds(60));
    }

    public TimedCache(Duration purgeDelay, Duration purgeInterval) {
        cache = new ConcurrentHashMap<>();
        purgeTimer = startNewPurgeTimer(purgeDelay, purgeInterval);
    }

    private Timer startNewPurgeTimer(Duration initialDelay, Duration interval) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long now = Instant.now().toEpochMilli();
                cache.entrySet().removeIf(a -> a.getValue() == null || a.getValue().expiredAt < now);
            }
        }, initialDelay.toMillis(), interval.toMillis());
        return timer;
    }

    public T get(String key) {
        if (closed) {
            return null;
        }
        TimedValue<T> v = cache.get(key);
        if (v == null) {
            return null;
        }
        return v.value;
    }

    public void put(String key, T value, long ttl) {
        if (closed) {
            return;
        }
        cache.put(key, new TimedValue<>(value, Instant.now().toEpochMilli() + ttl));
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (purgeTimer != null) {
            purgeTimer.cancel();
            purgeTimer = null;
        }
        cache.clear();
        cache = null;
    }

    static class TimedValue<T> {

        public T value;

        // in milliseconds
        public long expiredAt;

        public TimedValue(T value, long expiredAt) {
            this.value = value;
            this.expiredAt = expiredAt;
        }
    }
}
