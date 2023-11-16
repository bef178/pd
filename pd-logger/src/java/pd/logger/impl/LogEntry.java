package pd.logger.impl;

import pd.logger.LogLevel;
import pd.time.SimpleTime;

public class LogEntry {

    long timestamp;
    String hostname;
    LogLevel logLevel;
    String message;
    Object[] messageParams;
    Throwable throwable;

    public LogEntry() {
        timestamp = SimpleTime.now().findMillisecondsSinceEpoch();
        hostname = LogUtil.getHostname();
    }
}
