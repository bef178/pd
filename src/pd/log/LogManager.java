package pd.log;

import java.io.IOException;
import java.io.Writer;

import pd.time.TimeUtil;

/**
 * works as a holder of global logger
 */
public class LogManager {

    private static ILogger logger;

    private static LogLevel maxLogLevel;

    static {
        setMaxLogLevel(LogLevel.TRACE);
    }

    public static ILogger getLogger() {
        if (logger == null) {
            throw new RuntimeException("E: LogManager is not initialized");
        }
        return logger;
    }

    public static LogLevel getMaxLogLevel() {
        return maxLogLevel;
    }

    /**
     * initialization should happen in main thread at very beginning of main()
     */
    public static void setLogger(ILogger logger) {
        LogManager.logger = logger;
    }

    public static void setMaxLogLevel(LogLevel maxLogLevel) {
        LogManager.maxLogLevel = maxLogLevel;
    }

    public static void useConsoleLogger() {
        setLogger(ConsoleLogger.defaultInstance);
    }

    public static void useFileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        setLogger(new FileLogger(fileParent, filePrefix, numIntervalMilliseconds));
    }

    public static void useQueuedFileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        setLogger(new QueuedLogger(new FileLogger(fileParent, filePrefix, numIntervalMilliseconds)));
    }

    /**
     * every actual logger calls this to write
     */
    public static void writeLine(Writer w, String fieldSeparator, long timestamp, String hostname, LogLevel level,
            String message, Object... messageArguments) throws IOException {
        if (maxLogLevel == null) {
            // log is off
            return;
        }

        if (level.priority() > maxLogLevel.priority()) {
            return;
        }

        if (messageArguments != null && messageArguments.length > 0) {
            message = String.format(message, messageArguments);
        }

        // TODO csv
        w.write(TimeUtil.toUtcString(timestamp));
        w.write(fieldSeparator);
        w.write(hostname);
        w.write(fieldSeparator);
        w.write(level.toString());
        w.write(fieldSeparator);
        w.write(message);
        w.write('\n');
    }

    private LogManager() {
        // private dummy
    }
}
