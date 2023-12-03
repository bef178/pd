package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class Slf4jLoggerFactoryImpl implements ILoggerFactory {

    Slf4jLoggerImpl slf4jLogger = new Slf4jLoggerImpl();

    @Override
    public Logger getLogger(String name) {
        return slf4jLogger;
    }
}
