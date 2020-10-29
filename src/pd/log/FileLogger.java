package pd.log;

import static pd.log.LogManager.Util.getHostname;
import static pd.log.LogManager.Util.writeLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pd.time.TimeUtil;

public class FileLogger implements ILogger {

    private final String fileParent;
    private final String filePrefix;
    private final long numIntervalMilliseconds;

    public FileLogger(String fileParent, String filePrefix, long numIntervalMilliseconds) {
        this.fileParent = fileParent;
        this.filePrefix = filePrefix;
        this.numIntervalMilliseconds = numIntervalMilliseconds;
    }

    @Override
    public void flush() {
        // dummy
    }

    protected File getLogFile(long timestamp, LogLevel level) {
        timestamp -= timestamp % numIntervalMilliseconds;
        String timePart = TimeUtil.toUtcString("%04d%02d%02d%02d%02d%02dZ", timestamp);
        String logLevelPart = level.isPriorTo(LogLevel.INFO) ? "error" : "trace";
        String basename = String.format("%s_%s_%s.%s.log", filePrefix, timePart, getHostname(),
                logLevelPart);
        return new File(fileParent, basename);
    }

    @Override
    public void log(long timestamp, LogLevel level, String message) {
        File parent = new File(fileParent);
        if (!parent.exists()) {
            parent.mkdirs();
        } else if (!parent.isDirectory()) {
            throw new RuntimeException("NotDirectoryException: " + fileParent);
        }

        File logFile = getLogFile(timestamp, level);

        try (FileWriter w = new FileWriter(logFile, true)) {
            writeLine(w, ",", timestamp, getHostname(), level, message);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
