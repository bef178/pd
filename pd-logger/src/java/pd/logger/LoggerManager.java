package pd.logger;

import java.util.Objects;
import java.util.Properties;

import pd.logger.impl.ConsoleLogger;
import pd.logger.impl.DispatchLogger;
import pd.logger.impl.FileLogger;
import pd.util.ResourceExtension;

public class LoggerManager {

    private static final LoggerManager one = new LoggerManager();

    public static LoggerManager singleton() {
        return one;
    }

    private Logger logger = buildLoggerFromResource();

    private LoggerManager() {
        // private dummy
    }

    /**
     * will try to create a logger from properties file if logger is null
     */
    public Logger getLogger() {
        if (logger == null) {
            throw new IllegalArgumentException("E: LoggerManager has no logger");
        }
        return logger;
    }

    /**
     * initialization should happen in main thread at very beginning of main()
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ConsoleLogger buildConsoleLogger(LogLevel maxLogLevel) {
        if (maxLogLevel == ConsoleLogger.defaultInstance.maxLogLevel) {
            return ConsoleLogger.defaultInstance;
        } else {
            return new ConsoleLogger(maxLogLevel);
        }
    }

    public Logger buildLoggerFromResource() {
        final String resourceName = "pd.logger.properties";
        return buildLoggerFromResource(resourceName);
    }

    public Logger buildLoggerFromResource(String resourceName) {
        Properties properties = ResourceExtension.resourceAsPropertiesNoThrow(resourceName);
        if (properties == null) {
            return null;
        }
        try {
            return buildLogger(properties);
        } catch (IllegalArgumentException e) {
            ConsoleLogger.defaultInstance.logError("E: unacceptable property: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            ConsoleLogger.defaultInstance.logError("E: {}", e.getMessage());
            return null;
        }
    }

    private Logger buildLogger(Properties properties) {
        DispatchLogger dispatchLogger = new DispatchLogger();

        ConsoleLogger consoleLogger = buildConsoleLogger(properties);
        if (consoleLogger != null) {
            dispatchLogger.addLogger(consoleLogger);
        }

        FileLogger fileLogger = buildFileLogger(properties);
        if (fileLogger != null) {
            dispatchLogger.addLogger(fileLogger);
        }

        return dispatchLogger;
    }

    private ConsoleLogger buildConsoleLogger(Properties properties) {
        if (properties == null) {
            return null;
        }

        final boolean enabled;
        {
            final String key = "pd.logger.consoleLogger.enabled";
            String value = properties.getProperty(key);
            enabled = Objects.equals(value, "true");
        }
        if (!enabled) {
            return null;
        }

        final LogLevel maxLogLevel;
        {
            final String key = "pd.logger.consoleLogger.maxLogLevel";
            String value = properties.getProperty(key);
            if (value == null) {
                value = "info";
            }
            maxLogLevel = LogLevel.fromLiteral(value);
        }

        return buildConsoleLogger(maxLogLevel);
    }

    private FileLogger buildFileLogger(Properties properties) {
        if (properties == null) {
            return null;
        }

        final boolean enabled;
        {
            String key = "pd.logger.fileLogger.enabled";
            String value = properties.getProperty(key);
            enabled = Objects.equals(value, "true");
        }
        if (!enabled) {
            return null;
        }

        final LogLevel maxLogLevel;
        {
            final String key = "pd.logger.fileLogger.maxLogLevel";
            String value = properties.getProperty(key);
            if (value == null) {
                value = "info";
            }
            maxLogLevel = LogLevel.fromLiteral(value);
        }

        final String fileRoot;
        {
            final String key = "pd.logger.fileLogger.fileRoot";
            String value = properties.getProperty(key);
            if (value == null) {
                throw new IllegalArgumentException(String.format("`%s` is not present", key));
            }
            fileRoot = value;
        }

        final String filePrefix;
        {
            final String key = "pd.logger.fileLogger.filePrefix";
            String value = properties.getProperty(key);
            if (value == null) {
                throw new IllegalArgumentException(String.format("`%s` is not present", key));
            }
            filePrefix = value;
        }

        final int fileInterval;
        {
            final String key = "pd.logger.fileLogger.fileInterval";
            String value = properties.getProperty(key);
            if (value == null) {
                value = "600000";
            }
            final int intValue;
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("`%s` is not number", key), e);
            }
            fileInterval = intValue;
        }

        return new FileLogger(maxLogLevel, fileRoot, filePrefix, fileInterval);
    }
}
