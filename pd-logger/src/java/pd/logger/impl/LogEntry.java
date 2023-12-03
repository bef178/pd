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

    public static LogEntry make(LogLevel level, String message, Object... messageParams) {
        LogEntry logEntry = new LogEntry();
        logEntry.timestamp = SimpleTime.now().findMillisecondsSinceEpoch();
        logEntry.hostname = LogUtil.getHostname();
        logEntry.logLevel = level;
        logEntry.message = message;
        logEntry.messageParams = messageParams;
        if (messageParams != null && messageParams.length > 0) {
            Object last = messageParams[messageParams.length - 1];
            if (last instanceof Throwable) {
                logEntry.throwable = (Throwable) last;
            }
        }
        return logEntry;
    }
}
