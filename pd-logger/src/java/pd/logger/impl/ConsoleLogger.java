package pd.logger.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import pd.fenc.CurvePattern;
import pd.logger.Logger;
import pd.logger.LogLevel;
import pd.time.SimpleTime;

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
    public void log(LogLevel level, String message, Object... messageParams) {
        if (maxLogLevel == null || level.ordinal() > maxLogLevel.ordinal()) {
            return;
        }
        Writer w = level.ordinal() < LogLevel.INFO.ordinal() ? errWriter : outWriter;
        try {
            LoggerUtil.writeLine(w, SimpleTime.now().findMillisecondsSinceEpoch(), LoggerUtil.getHostname(), level, CurvePattern.format(message, messageParams));
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
