package pd.log;

public enum LogLevel {
    ERROR, // recoverable unexpected exception and unrecoverable fatal error
    WARNING, // panic, potentially harmful
    INFO,
    PERFORMANCE,
    TRACE;

    private static final LogLevel[] values = LogLevel.values();

    public static final LogLevel fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }

    public static LogLevel getMaxLogLevel() {
        return LogLevel.TRACE;
    }

    public boolean isPriorTo(LogLevel level) {
        return ordinal() < level.ordinal();
    }
}
