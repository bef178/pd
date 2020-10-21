package pd.log;

public enum LogLevel {
    FATAL, // application aborts
    ERROR, // throws exception but recoverable
    WARNING, // panic, potentially harmful
    INFO,
    PERFORMANCE,
    DEBUG,
    TRACE;

    public int priority() {
        return ordinal();
    }
}
