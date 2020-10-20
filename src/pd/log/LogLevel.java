package pd.log;

public enum LogLevel {
    QUIET,
    FATAL, // application aborts
    ERROR, // throws exception but recoverable
    WARNING, // panic, potentially harmful
    INFO,
    PERFORMANCE,
    DEBUG,
    ALL,
}
