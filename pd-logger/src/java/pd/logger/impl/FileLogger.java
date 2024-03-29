package pd.logger.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pd.logger.LogLevel;
import pd.time.TimeExtension;

public class FileLogger extends ThreadedLogger {

    private final LogLevel maxLogLevel;
    private final String fileRoot;
    private final String filePrefix;
    private final long fileInterval;

    public FileLogger(LogLevel maxLogLevel, String fileRoot, String filePrefix, int fileInterval) {
        this.maxLogLevel = maxLogLevel;
        this.fileRoot = fileRoot;
        this.filePrefix = filePrefix;
        this.fileInterval = fileInterval;
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return maxLogLevel != null && level != null && level.ordinal() <= maxLogLevel.ordinal();
    }

    @Override
    protected void doLog(LogEntry logEntry) {
        File dir = new File(fileRoot);
        if (!dir.exists()) {
            dir.mkdirs();
        } else if (!dir.isDirectory()) {
            throw new RuntimeException(String.format("[%s] is not directory", fileRoot));
        }

        File logFile = new File(fileRoot, buildBasename(logEntry.timestamp, logEntry.hostname, logEntry.logLevel));

        try (FileWriter w = new FileWriter(logFile, true)) {
            LogUtil.writeLine(w, logEntry);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String buildBasename(long timestamp, String hostname, LogLevel level) {
        timestamp -= timestamp % fileInterval;
        String timePart = TimeExtension.toUtcString(timestamp, "%04d%02d%02dT%02d%02d%02dZ");
        String logLevelPart = level.ordinal() <= LogLevel.WARNING.ordinal() ? "warning" : "verbose";
        if (filePrefix == null || filePrefix.isEmpty()) {
            return String.format("%s_%s.%s.log", timePart, hostname, logLevelPart);
        } else {
            return String.format("%s_%s_%s.%s.log", filePrefix, timePart, hostname, logLevelPart);
        }
    }
}
