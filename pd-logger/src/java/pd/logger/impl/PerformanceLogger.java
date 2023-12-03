package pd.logger.impl;

import java.io.Closeable;

import pd.logger.Logger;
import pd.logger.LogLevel;
import pd.time.SimpleTime;

public class PerformanceLogger implements Closeable {

    private final Logger logger;

    private final String tag;

    private long startTimestamp;
    private long endTimestamp;

    public PerformanceLogger(Logger logger, String tag) {
        this.logger = logger;
        this.tag = tag;
        restart();
    }

    @Override
    public void close() {
        if (endTimestamp == 0) {
            endTimestamp = SimpleTime.now().findMillisecondsSinceEpoch();
        }

        if (logger != null) {
            logger.log(LogLevel.INFO, null, "%s: total %ld millisecond(s)", tag, endTimestamp - startTimestamp);
        }
    }

    public void restart() {
        startTimestamp = SimpleTime.now().findMillisecondsSinceEpoch();
    }
}
