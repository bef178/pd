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

    @Override
    public boolean isEnabled(LogLevel level) {
        for (Logger logger : subscribers) {
            if (logger.isEnabled(level)) {
                return true;
            }
        }
        return false;
    }

    public void addLogger(Logger logger) {
        subscribers.add(logger);
    }
}
