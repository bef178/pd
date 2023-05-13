package pd.logger.impl;

import java.util.LinkedHashSet;

import pd.logger.Logger;
import pd.logger.LogLevel;

public class DispatchLogger implements Logger {

    LinkedHashSet<Logger> subscribers = new LinkedHashSet<>();

    @Override
    public void flush() {
        for (Logger logger : subscribers) {
            logger.flush();
        }
    }

    @Override
    public void log(LogLevel level, String message, Object... messageParams) {
        for (Logger logger : subscribers) {
            logger.log(level, message, messageParams);
        }
    }

    public void addLogger(Logger logger) {
        subscribers.add(logger);
    }
}
