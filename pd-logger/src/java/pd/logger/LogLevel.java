package pd.logger;

public enum LogLevel {
    ERROR, // recoverable unexpected exception and unrecoverable fatal error
    WARNING, // panic, unusual state, potentially harmful
    INFO,
    VERBOSE; // debug, trace

    public static final LogLevel MAX_LEVEL = LogLevel.VERBOSE;

    private static final LogLevel[] values = LogLevel.values();

    public static LogLevel fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }

    public static LogLevel fromLiteral(String name) {
        if (name == null) {
            return null;
        }
        return LogLevel.valueOf(name.toUpperCase()); // let it throw
    }
}
