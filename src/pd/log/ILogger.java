package pd.log;

import static pd.log.Util.evaluateMessage;

import java.io.Closeable;
import java.io.IOException;

import pd.time.TimeUtil;

public interface ILogger extends Closeable {

    @Override
    public default void close() throws IOException {
        flush();
    }

    public void flush();

    public LogLevel getMaxAcceptableLogLevel();

    public default void log(LogLevel level, String message, Object... messageArguments) {
        log(TimeUtil.now(), level, message, messageArguments);
    }

    public void log(long timestamp, LogLevel level, String message);

    public default void log(long timestamp, LogLevel level, String message,
            Object... messageArguments) {
        message = evaluateMessage(message, messageArguments);
        log(timestamp, level, message);
    }

    public default void logError(String message, Object... messageArguments) {
        log(LogLevel.ERROR, message, messageArguments);
    }

    public default void logInfo(String message, Object... messageArguments) {
        log(LogLevel.INFO, message, messageArguments);
    }

    public default void logTrace(String message, Object... messageArguments) {
        log(LogLevel.TRACE, message, messageArguments);
    }

    public default void logWarning(String message, Object... messageArguments) {
        log(LogLevel.WARNING, message, messageArguments);
    }
}
