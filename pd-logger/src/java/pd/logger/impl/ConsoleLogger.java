package pd.logger.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import pd.logger.Logger;
import pd.logger.LogLevel;

public class ConsoleLogger implements Logger {

    public static final ConsoleLogger defaultInstance = new ConsoleLogger(LogLevel.INFO);

    private static final Writer outWriter = new PrintWriter(System.out, true);
    private static final Writer errWriter = new PrintWriter(System.err, true);

    public final LogLevel maxLogLevel;

    public ConsoleLogger(LogLevel maxLogLevel) {
        this.maxLogLevel = maxLogLevel;
    }

    @Override
    public void flush() {
        try {
            errWriter.flush();
            outWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(LogLevel level, Throwable throwable, String message, Object... messageParams) {
        if (!isEnabled(level)) {
            return;
        }

        LogEntry logEntry = new LogEntry();
        logEntry.logLevel = level;
        logEntry.message = message;
        logEntry.messageParams = messageParams;
        logEntry.throwable = throwable;

        Writer w = level.ordinal() < LogLevel.INFO.ordinal() ? errWriter : outWriter;
        try {
            LogUtil.writeLine(w, logEntry);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return maxLogLevel != null && level != null && level.ordinal() <= maxLogLevel.ordinal();
    }
}
