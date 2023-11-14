package pd.logger;

public interface Logger {

    default void error(String message, Object... messageParams) {
        log(LogLevel.ERROR, message, messageParams);
    }

    default void info(String message, Object... messageParams) {
        log(LogLevel.INFO, message, messageParams);
    }

    default void warning(String message, Object... messageParams) {
        log(LogLevel.WARNING, message, messageParams);
    }

    default void verbose(String message, Object... messageParams) {
        log(LogLevel.VERBOSE, message, messageParams);
    }

    void log(LogLevel level, String message, Object... messageParams);

    void flush();
}
