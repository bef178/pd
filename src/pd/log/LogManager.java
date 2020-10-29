package pd.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import pd.time.TimeUtil;

/**
 * works as a holder of global logger
 */
public class LogManager {

    private static class Config {

        private static final String KEY_PREFIX = LogManager.class.getCanonicalName();
        private static final String KEY_LOGGER_CLASS = KEY_PREFIX + ".loggerClass";
        private static final String KEY_FILELOGGER_FILEPARENT = KEY_PREFIX + ".fileLogger.fileParent";
        private static final String KEY_FILELOGGER_FILEPREFIX = KEY_PREFIX + ".fileLogger.filePrefix";
        private static final String KEY_FILELOGGER_FILEINTERVAL = KEY_PREFIX + ".fileLogger.fileInterval";

        public static final String CONFIG_FILE = KEY_PREFIX + ".properties";

        public static ILogger createDefaultLogger() {
            Properties props = Util.loadProperties(CONFIG_FILE);
            return createLogger(props);
        }

        public static ILogger createLogger(Properties props) {
            if (props == null) {
                return null;
            }

            String loggerClass = props.getProperty(KEY_LOGGER_CLASS);
            if (loggerClass == null) {
                return null;
            }

            boolean hasWrapper = false;
            if (loggerClass.startsWith("Queued")) {
                hasWrapper = true;
                loggerClass = loggerClass.substring("Queued".length());
            }

            if (loggerClass.equals(ConsoleLogger.class.getSimpleName())) {
                ILogger logger = ConsoleLogger.defaultInstance;
                return hasWrapper ? new QueuedLogger(logger) : logger;
            } else if (loggerClass.equals(FileLogger.class.getSimpleName())) {
                String fileParent = props.getProperty(KEY_FILELOGGER_FILEPARENT);
                String filePrefix = props.getProperty(KEY_FILELOGGER_FILEPREFIX);
                String fileInterval = props.getProperty(KEY_FILELOGGER_FILEINTERVAL);
                if (fileParent == null || fileParent.isEmpty()
                        || filePrefix == null || filePrefix.isEmpty()
                        || fileInterval == null || fileInterval.isEmpty()) {
                    return null;
                }
                int numIntervalMilliseconds;
                try {
                    numIntervalMilliseconds = Integer.parseInt(fileInterval);
                } catch (NumberFormatException e) {
                    return null;
                }
                ILogger logger = new FileLogger(fileParent, filePrefix, numIntervalMilliseconds);
                return hasWrapper ? new QueuedLogger(logger) : logger;
            }
            return null;
        }
    }

    public static class Util {

        public static String evaluateMessage(String message, Object... messageArguments) {
            if (messageArguments != null && messageArguments.length > 0) {
                message = String.format(message, messageArguments);
            }
            return message;
        }

        public static String getHostname() {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return "UnknownHostExceptionHostname";
            } catch (Exception e) {
                return "ExceptionHostname";
            }
        }

        private static InputStream getInputStream(String file) {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            return currentClassLoader != null
                    ? currentClassLoader.getResourceAsStream(file)
                    : ClassLoader.getSystemResourceAsStream(file);
        }

        public static Properties loadProperties(String file) {
            try (InputStream stream = getInputStream(file)) {
                if (stream != null) {
                    Properties props = new Properties();
                    props.load(stream);
                    return props;
                }
            } catch (IOException e) {
                // dummy
            }
            return null;
        }

        /**
         * actual logger would call this to write
         */
        public static void writeLine(Writer w, String fieldSeparator, long timestamp, String hostname, LogLevel level,
                String message) throws IOException {
            if (maxLogLevel == null) {
                // log is off
                return;
            }

            if (level.priority() > maxLogLevel.priority()) {
                return;
            }

            synchronized (lock) {
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
        }
    }

    private static final Object lock = new Object();

    private static ILogger logger;

    private static LogLevel maxLogLevel;

    static {
        setMaxLogLevel(LogLevel.TRACE);
    }

    /**
     * will try to create a logger from properties file if logger is null
     */
    public static ILogger getLogger() {
        if (logger == null) {
            synchronized (lock) {
                if (logger == null) {
                    logger = Config.createDefaultLogger();
                }
            }
        }
        if (logger == null) {
            throw new RuntimeException("E: LogManager.logger is not set");
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

    private LogManager() {
        // private dummy
    }
}
