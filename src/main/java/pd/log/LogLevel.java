package pd.log;

public enum LogLevel {
    ERROR, // recoverable unexpected exception and unrecoverable fatal error
    WARNING, // panic, unusual state, potentially harmful
    INFO,
    PERFORMANCE,
    VERBOSE; // debug, trace

    public static final LogLevel MAX_LEVEL = LogLevel.VERBOSE;

    private static final LogLevel[] values = LogLevel.values();

    public static final LogLevel fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }
}
