package pd.logger;

import java.util.Properties;

import pd.util.ResourceExtension;

public class LoggerManager {

    private static class Config {

        private static final String KEY_PREFIX = LoggerManager.class.getPackage().getName();
        private static final String KEY_LOGGER_CLASS = KEY_PREFIX + ".loggerClass";
        private static final String KEY_LOG_LEVEL = KEY_PREFIX + ".loggerLevel";
        private static final String KEY_FILE_LOGGER_ROOT = KEY_PREFIX + ".fileLogger.root";
        private static final String KEY_FILE_LOGGER_PREFIX = KEY_PREFIX + ".fileLogger.prefix";
        private static final String KEY_FILE_LOGGER_INTERVAL = KEY_PREFIX + ".fileLogger.interval";

        public static final String PROPS_FILE = KEY_PREFIX + ".properties";

        /**
         * return null if any unacceptable values
         */
        public static ILogger createLoggerByProps() {
            Properties props = ResourceExtension.resourceAsPropertiesNoThrow(PROPS_FILE);
            try {
                return createLoggerByProps(props);
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

            String loggerLevelString = props.getProperty(KEY_LOG_LEVEL);
            if (loggerLevelString == null) {
                throw new IllegalArgumentException(KEY_LOG_LEVEL + " not found");
            } else if (loggerLevelString.equals("OFF") || loggerLevelString.equals("MUTE")) {
                return new ConsoleLogger(null);
            }

            // let it down if there is exception
            LogLevel maxLevel;
            try {
                maxLevel = LogLevel.valueOf(loggerLevelString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(KEY_LOG_LEVEL + "=" + loggerLevelString);
            }

            String loggerClass = props.getProperty(KEY_LOGGER_CLASS);
            if (loggerClass == null) {
                throw new IllegalArgumentException(KEY_LOGGER_CLASS + " not found");
            }

            if (loggerClass.equals(ConsoleLogger.class.getSimpleName())) {
                return maxLevel == ConsoleLogger.defaultInstance.maxLevel
                        ? ConsoleLogger.defaultInstance
                        : new ConsoleLogger(maxLevel);
            } else if (loggerClass.equals(FileLogger.class.getSimpleName())) {
                String rootString = props.getProperty(KEY_FILE_LOGGER_ROOT);
                if (rootString == null) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_ROOT + " not found");
                } else if (rootString.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_ROOT + "=" + rootString);
                }

                String prefixString = props.getProperty(KEY_FILE_LOGGER_PREFIX);
                if (prefixString == null) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_PREFIX + " not found");
                } else if (prefixString.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_PREFIX + "=" + prefixString);
                }

                String intervalString = props.getProperty(KEY_FILE_LOGGER_INTERVAL);
                if (intervalString == null) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_INTERVAL + " not found");
                } else if (intervalString.isEmpty()) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_INTERVAL + "=" + intervalString);
                }
                // if it throws exception, let it down
                int interval;
                try {
                    interval = Integer.parseInt(intervalString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(KEY_FILE_LOGGER_INTERVAL + "=" + intervalString);
                }

                return new FileLogger(rootString, prefixString, interval, maxLevel);
            } else {
                throw new IllegalArgumentException(KEY_LOGGER_CLASS + "=" + loggerClass);
            }
        }
    }

    private static volatile ILogger logger = Config.createLoggerByProps();;

    /**
     * will try to create a logger from properties file if logger is null
     */
    public static ILogger getLogger() {
        if (logger == null) {
            throw new IllegalArgumentException("E: LoggerManager.logger is not set");
        }
        return logger;
    }

    /**
     * initialization should happen in main thread at very beginning of main()
     */
    public static void setLogger(ILogger logger) {
        LoggerManager.logger = logger;
    }

    public static void useConsoleLogger() {
        setLogger(ConsoleLogger.defaultInstance);
    }

    public static void useFileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        setLogger(new FileLogger(fileParent, filePrefix, numIntervalMilliseconds));
    }

    private LoggerManager() {
        // private dummy
    }
}
