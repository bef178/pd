package pd.logger;

public interface Logger {

    default void error(String message, Object... messageParams) {
        error(null, message, messageParams);
    }

    default void error(Throwable throwable, String message, Object... messageParams) {
        log(LogLevel.ERROR, throwable, message, messageParams);
    }

    default void info(String message, Object... messageParams) {
        info(null, message, messageParams);
    }

    default void info(Throwable throwable, String message, Object... messageParams) {
        log(LogLevel.INFO, throwable, message, messageParams);
    }

    default void warning(String message, Object... messageParams) {
        warning(null, message, messageParams);
    }

    default void warning(Throwable throwable, String message, Object... messageParams) {
        log(LogLevel.WARNING, throwable, message, messageParams);
    }

    default void verbose(String message, Object... messageParams) {
        verbose(null, message, messageParams);
    }

    default void verbose(Throwable throwable, String message, Object... messageParams) {
        log(LogLevel.VERBOSE, throwable, message, messageParams);
    }

    void log(LogLevel level, Throwable throwable, String message, Object... messageParams);

    boolean isEnabled(LogLevel level);

    void flush();
}
