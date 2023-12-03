package org.slf4j.impl;

import org.slf4j.Marker;
import pd.logger.LogLevel;
import pd.logger.Logger;
import pd.logger.LoggerManager;

public class Slf4jLoggerImpl implements org.slf4j.Logger {

    static Logger logger = LoggerManager.singleton().getLogger();

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(LogLevel.VERBOSE);
    }

    @Override
    public void trace(String msg) {
        logger.verbose(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.verbose(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.verbose(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.verbose(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.verbose(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.VERBOSE);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.verbose(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.verbose(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.verbose(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.verbose(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.verbose(msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(LogLevel.VERBOSE);
    }

    @Override
    public void debug(String msg) {
        logger.verbose(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.verbose(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.verbose(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.verbose(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.verbose(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.VERBOSE);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.verbose(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.verbose(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.verbose(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.verbose(format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.verbose(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(LogLevel.WARNING);
    }

    @Override
    public void warn(String msg) {
        logger.warning(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warning(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warning(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warning(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warning(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.WARNING);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warning(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warning(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warning(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warning(format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warning(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(msg, t);
    }
}
