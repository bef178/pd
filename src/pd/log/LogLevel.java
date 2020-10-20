package pd.log;

public enum LogLevel {
    OFF,
    FATAL, // application aborts
    ERROR, // throws exception but recoverable
    WARNING, // panic, potentially harmful
    INFO,
    PERFORMANCE,
    DEBUG,
    TRACE,
    ALL;

    public int priority() {
        return ordinal();
    }
}
