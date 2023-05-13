package pd.logger.impl;

import pd.logger.LogLevel;

public class LogEntry {
    long timestamp;
    String hostname;
    LogLevel logLevel;
    String message;
    Object[] messageParams;
}
