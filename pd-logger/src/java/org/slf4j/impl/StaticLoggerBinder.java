package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

// for 1.7.x
public final class StaticLoggerBinder implements LoggerFactoryBinder {

    private final Slf4jLoggerFactoryImpl slf4jLoggerFactory = new Slf4jLoggerFactoryImpl();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return slf4jLoggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return Slf4jLoggerFactoryImpl.class.getName();
    }
}
