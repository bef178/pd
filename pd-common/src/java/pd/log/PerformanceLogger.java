package pd.log;

import java.io.Closeable;

import pd.time.SimpleTime;

public class PerformanceLogger implements Closeable {

    private final ILogger logger;

    private final String tag;

    private long startTimestamp;
    private long endTimestamp;

    public PerformanceLogger(ILogger logger, String tag) {
        this.logger = logger;
        this.tag = tag;
        restart();
    }

    @Override
    public void close() {
        if (endTimestamp == 0) {
            endTimestamp = SimpleTime.now().getMillisecondsSinceEpoch();
        }

        if (logger != null) {
            logger.log(LogLevel.PERFORMANCE, "%s: total %ld millisecond(s)", tag, endTimestamp - startTimestamp);
        }
    }

    public void restart() {
        startTimestamp = SimpleTime.now().getMillisecondsSinceEpoch();
    }
}
