package pd.logger;

import lombok.Data;
import pd.logger.impl.ConsoleLogger;
import pd.logger.impl.Dispatcher;
import pd.logger.impl.FileLogger;

@Data
public class LoggerManager {

    private static final LoggerManager one = new LoggerManager();

    public static LoggerManager singleton() {
        return one;
    }

    private Logger logger;

    public LoggerManager() {
        this(new Dispatcher()
                .addLogger(ConsoleLogger.defaultInstance)
                .addLogger(new FileLogger(LogLevel.INFO, "./var/log", "log", 1000 * 60 * 10)));
    }

    public LoggerManager(Logger logger) {
        this.logger = logger;
    }
}
