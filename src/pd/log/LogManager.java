package pd.log;

import java.util.Properties;

public class LogManager {

    private static class Config {

        private static final String KEY_PREFIX = LogManager.class.getPackageName();
        private static final String KEY_LOGGERCLASS = KEY_PREFIX + ".loggerClass";
        private static final String KEY_LOGLEVEL = KEY_PREFIX + ".loggerLevel";
        private static final String KEY_FILELOGGER_ROOT = KEY_PREFIX + ".fileLogger.root";
        private static final String KEY_FILELOGGER_PREFIX = KEY_PREFIX + ".fileLogger.prefix";
        private static final String KEY_FILELOGGER_INTERVAL = KEY_PREFIX + ".fileLogger.interval";

        public static final String PROPS_FILE = KEY_PREFIX + ".properties";

        /**
         * return null if any unacceptable values
         */
        public static ILogger createLoggerByProps() {
            Properties props = LogUtil.loadProperties(PROPS_FILE);
            try {
                ILogger logger = createLoggerByProps(props);
                return logger;
            } catch (IllegalArgumentException e) {
                ConsoleLogger.defaultInstance.logError("E: unacceptable property: {}", e.getMessage());
                return null;
            } catch (Exception e) {
                ConsoleLogger.defaultInstance.logError("E: {}", e.getMessage());
                return null;
            }
        }

        public static ILogger createLoggerByProps(Properties props) {
            if (props == null) {
                return null;
            }

            String loggerLevelString = props.getProperty(KEY_LOGLEVEL);
            if (loggerLevelString == null) {
                throw new IllegalArgumentException(KEY_LOGLEVEL + " not found");
            } else if (loggerLevelString.equals("OFF") || loggerLevelString.equals("MUTE")) {
                return new ConsoleLogger(null);
            }

            // let it down if there is exception
            LogLevel maxLevel;
            try {
                maxLevel = LogLevel.valueOf(loggerLevelString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(KEY_LOGLEVEL + "=" + loggerLevelString);
            }

            String loggerClass = props.getProperty(KEY_LOGGERCLASS);
            if (loggerClass == null) {
                throw new IllegalArgumentException(KEY_LOGGERCLASS + " not found");
            }

            if (loggerClass.equals(ConsoleLogger.class.getSimpleName())) {
                return maxLevel == ConsoleLogger.defaultInstance.maxLevel
                        ? ConsoleLogger.defaultInstance
                        : new ConsoleLogger(maxLevel);
            } else if (loggerClass.equals(FileLogger.class.getSimpleName())) {
                String rootString = props.getProperty(KEY_FILELOGGER_ROOT);
                if (rootString == null) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_ROOT + " not found");
                } else if (rootString.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_ROOT + "=" + rootString);
                }

                String prefixString = props.getProperty(KEY_FILELOGGER_PREFIX);
                if (prefixString == null) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_PREFIX + " not found");
                } else if (prefixString.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_PREFIX + "=" + prefixString);
                }

                String intervalString = props.getProperty(KEY_FILELOGGER_INTERVAL);
                if (intervalString == null) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_INTERVAL + " not found");
                } else if (intervalString.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_INTERVAL + "=" + intervalString);
                }
                // if it throws exception, let it down
                int interval;
                try {
                    interval = Integer.parseInt(intervalString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(KEY_FILELOGGER_INTERVAL + "=" + intervalString);
                }

                return new FileLogger(rootString, prefixString, interval, maxLevel);
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
                    logger = Config.createLoggerByProps();
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

    private LogManager() {
        // private dummy
    }
}
