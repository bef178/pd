package pd.logger;

public interface Logger {

    void flush();

    void log(LogLevel level, String message, Object... messageParams);

    default void logError(String message, Object... messageParams) {
        log(LogLevel.ERROR, message, messageParams);
    }

    default void logInfo(String message, Object... messageParams) {
        log(LogLevel.INFO, message, messageParams);
    }

    default void logVerbose(String message, Object... messageParams) {
        log(LogLevel.VERBOSE, message, messageParams);
    }

    default void logWarning(String message, Object... messageParams) {
        log(LogLevel.WARNING, message, messageParams);
    }
}
