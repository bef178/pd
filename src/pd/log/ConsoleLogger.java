package pd.log;

import static pd.log.LogUtil.evaluateMessage;
import static pd.log.LogUtil.getHostname;
import static pd.log.LogUtil.writeLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import pd.time.Ctime;

public class ConsoleLogger implements ILogger {

    public static final ConsoleLogger defaultInstance = new ConsoleLogger(LogLevel.INFO);

    private static final Writer outWriter = new PrintWriter(System.out, true);
    private static final Writer errWriter = new PrintWriter(System.err, true);

    public final LogLevel maxLevel;

    public ConsoleLogger(LogLevel maxLevel) {
        this.maxLevel = maxLevel;
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
    public void log(LogLevel level, String message, Object... messageArguments) {
        if (level.ordinal() > maxLevel.ordinal()) {
            return;
        }
        Writer w = level.ordinal() < LogLevel.INFO.ordinal() ? errWriter : outWriter;
        try {
            writeLine(w, ",", Ctime.now(), getHostname(), level, evaluateMessage(message, messageArguments));
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
