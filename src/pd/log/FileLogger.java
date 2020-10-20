package pd.log;

import static pd.log.LogManager.writeLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import pd.time.TimeUtil;

public class FileLogger implements ILogger {

    private final LogLevel maxLevel;

    private String fileParent;
    private String filePrefix;
    private long numIntervalMilliseconds;

    public FileLogger(LogLevel maxLevel, String fileParent, String filePrefix, long numIntervalMilliseconds) {
        this.maxLevel = maxLevel;
        this.fileParent = fileParent;
        this.filePrefix = filePrefix;
        this.numIntervalMilliseconds = numIntervalMilliseconds;
    }

    @Override
    public void flush() {
        // dummy
    }

    private File getDstFile(long timestamp, LogLevel level) {
        timestamp -= timestamp % numIntervalMilliseconds;
        String timePart = TimeUtil.toUtcString("%04d%02d%02d%02d%02d%02dZ", timestamp);
        String basename = String.format("%s_%s_%s.%s.log", filePrefix, timePart, getHostname(),
                level.toString().toLowerCase());
        return new File(fileParent, basename);
    }

    @Override
    public void log(long timestamp, LogLevel level, String message, Object... messageArguments) {
        if (level.ordinal() > maxLevel.ordinal()) {
            return;
        }

        File parent = new File(fileParent);
        if (!parent.exists()) {
            parent.mkdirs();
        } else if (!parent.isDirectory()) {
            throw new RuntimeException("NotDirectoryException: " + fileParent);
        }

        File dstFile = getDstFile(timestamp, level);

        try (FileWriter w = new FileWriter(dstFile, true)) {
            writeLine(w, ",", timestamp, getHostname(), level, message, messageArguments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
