package pd.log;

public enum LogLevel {
    ERROR, // recoverable unexpected exception and unrecoverable fatal error
    WARNING, // panic, potentially harmful
    INFO,
    PERFORMANCE,
    TRACE;

    public boolean isPriorTo(LogLevel level) {
        return ordinal() < level.ordinal();
    }
}
