package pd.log;

import java.io.Closeable;
import java.io.IOException;

import pd.time.TimeUtil;

public interface ILogger extends Closeable {

    @Override
    public default void close() throws IOException {
        flush();
    }

    public void flush();

    public default void log(LogLevel level, String message, Object... messageArguments) {
        log(TimeUtil.now(), level, message, messageArguments);
    }

    public void log(long timestamp, LogLevel level, String message, Object... messageArguments);

    public default void logError(String message, Object... messageArguments) {
        log(LogLevel.ERROR, message, messageArguments);
    }

    public default void logInfo(String message, Object... messageArguments) {
        log(LogLevel.INFO, message, messageArguments);
    }
}
