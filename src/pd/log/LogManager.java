package pd.log;

import java.io.IOException;
import java.io.Writer;

import pd.time.TimeUtil;

/**
 * works as a holder of global logger
 */
public class LogManager {

    private static ILogger logger = null;

    public static ILogger getLogger() {
        if (logger == null) {
            throw new RuntimeException("E: LogManager is not initialized");
        }
        return logger;
    }

    /**
     * initialization should happen in main thread at very beginning of main()
     */
    public static void setLogger(ILogger logger) {
        LogManager.logger = logger;
    }

    public static void useConsoleLogger() {
        setLogger(ConsoleLogger.defaultInstance);
    }

    public static void useFileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        setLogger(new FileLogger(LogLevel.ALL, fileParent, filePrefix, numIntervalMilliseconds));
    }

    public static void useQueuedFileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        setLogger(new QueuedLogger(new FileLogger(LogLevel.ALL, fileParent, filePrefix, numIntervalMilliseconds)));
    }

    /**
     * every logger calls this to write
     */
    public static void writeLine(Writer w, String fieldSeparator, long timestamp, String hostname, LogLevel level,
            String message, Object... messageArguments) throws IOException {

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
