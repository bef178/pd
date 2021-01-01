package pd.log;

import java.util.Properties;

/**
 * works as a holder of global logger
 */
public class LogManager {

    private static class Config {

        private static final String KEY_PREFIX = LogManager.class.getCanonicalName();
        private static final String KEY_LOGGERCLASS = KEY_PREFIX + ".loggerClass";
        private static final String KEY_LOGLEVEL = KEY_PREFIX + ".logLevel";
        private static final String KEY_FILELOGGER_FILEPARENT = KEY_PREFIX + ".fileLogger.fileParent";
        private static final String KEY_FILELOGGER_FILEPREFIX = KEY_PREFIX + ".fileLogger.filePrefix";
        private static final String KEY_FILELOGGER_FILEINTERVAL = KEY_PREFIX + ".fileLogger.fileInterval";

        public static final String CONFIG_FILE = KEY_PREFIX + ".properties";

        /**
         * return null if any unacceptable values
         */
        public static ILogger createDefaultLogger() {
            Properties props = Util.loadProperties(CONFIG_FILE);
            try {
                return createLogger(props);
            } catch (IllegalArgumentException e) {
                ConsoleLogger.defaultInstance.logError("E: unacceptable property: %s", e.getMessage());
                return null;
            } catch (Exception e) {
                ConsoleLogger.defaultInstance.logError("E: %s", e.getMessage());
                return null;
            }
        }

        public static ILogger createLogger(Properties props) {
            if (props == null) {
                return null;
            }

            String logLevelString = props.getProperty(KEY_LOGLEVEL);
            if (logLevelString == null) {
                throw new IllegalArgumentException(KEY_LOGLEVEL + " not found");
            } else if (logLevelString.equals("OFF") || logLevelString.equals("MUTE")) {
                return new ConsoleLogger(null);
            }

            // if it throws exception, let it down
            LogLevel maxAcceptableLogLevel;
            try {
                maxAcceptableLogLevel = LogLevel.valueOf(logLevelString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(KEY_LOGLEVEL + "=" + logLevelString);
            }

            String loggerClass = props.getProperty(KEY_LOGGERCLASS);
            if (loggerClass == null) {
                throw new IllegalArgumentException(KEY_LOGGERCLASS + " not found");
            }

            boolean hasWrapper = false;
            if (loggerClass.startsWith("Queued")) {
                hasWrapper = true;
                loggerClass = loggerClass.substring("Queued".length());
            }

            if (loggerClass.equals(ConsoleLogger.class.getSimpleName())) {
                ILogger logger = maxAcceptableLogLevel == ConsoleLogger.defaultInstance.getMaxAcceptableLogLevel()
                        ? ConsoleLogger.defaultInstance
                        : new ConsoleLogger(maxAcceptableLogLevel);
                return hasWrapper ? new QueuedLogger(logger) : logger;
            } else if (loggerClass.equals(FileLogger.class.getSimpleName())) {
                String fileParent = props.getProperty(KEY_FILELOGGER_FILEPARENT);
                if (fileParent == null) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEPARENT + " not found");
                } else if (fileParent.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEPARENT + "=" + fileParent);
                }

                String filePrefix = props.getProperty(KEY_FILELOGGER_FILEPREFIX);
                if (filePrefix == null) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEPREFIX + " not found");
                } else if (filePrefix.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEPREFIX + "=" + filePrefix);
                }

                String fileInterval = props.getProperty(KEY_FILELOGGER_FILEINTERVAL);
                if (fileInterval == null) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEINTERVAL + " not found");
                } else if (fileInterval.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEINTERVAL + "=" + fileInterval);
                }
                // if it throws exception, let it down
                int numIntervalMilliseconds;
                try {
                    numIntervalMilliseconds = Integer.parseInt(fileInterval);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_FILEINTERVAL + "=" + fileInterval);
                }

                ILogger logger = new FileLogger(fileParent, filePrefix, numIntervalMilliseconds, maxAcceptableLogLevel);
                return hasWrapper ? new QueuedLogger(logger) : logger;
            }
            throw new IllegalArgumentException(KEY_LOGGERCLASS + "=" + loggerClass);
        }
    }

    private static final Object lock = new Object();

    private static ILogger logger;

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
            throw new IllegalArgumentException("E: LogManager.logger is not set");
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
        setLogger(new FileLogger(fileParent, filePrefix, numIntervalMilliseconds));
    }

    public static void useQueuedFileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        setLogger(new QueuedLogger(new FileLogger(fileParent, filePrefix, numIntervalMilliseconds)));
    }

    private LogManager() {
        // private dummy
    }
}
