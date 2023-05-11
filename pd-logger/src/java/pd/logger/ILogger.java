package pd.logger;

import java.io.Closeable;
import java.io.IOException;

public interface ILogger extends Closeable {

    @Override
    public default void close() throws IOException {
        flush();
    }

    public void flush();

    public void log(LogLevel level, String message, Object... messageArguments);

    public default void logError(String message, Object... messageArguments) {
        log(LogLevel.ERROR, message, messageArguments);
    }

    public default void logInfo(String message, Object... messageArguments) {
        log(LogLevel.INFO, message, messageArguments);
    }

    public default void logVerbose(String message, Object... messageArguments) {
        log(LogLevel.VERBOSE, message, messageArguments);
    }

    public default void logWarning(String message, Object... messageArguments) {
        log(LogLevel.WARNING, message, messageArguments);
    }
}
