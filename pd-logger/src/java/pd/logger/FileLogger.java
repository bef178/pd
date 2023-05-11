package pd.logger;

import static pd.logger.LoggerUtil.getHostname;
import static pd.logger.LoggerUtil.writeLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import pd.time.SimpleTime;
import pd.time.TimeExtension;

/**
 * FileLogger holds a thread writing down records to avoid latency from IO
 */
public class FileLogger implements ILogger {

    class Record {
        long timestamp;
        LogLevel level;
        String message;
        Object[] messageArguments;
    }

    class ThreadedLogWriter {

        private final String logPrefix = ThreadedLogWriter.class.getSimpleName();

        private final ILogger logger = ConsoleLogger.defaultInstance;

        private final BlockingQueue<Record> queue = new LinkedBlockingQueue<Record>();

        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private final AtomicBoolean isStopped = new AtomicBoolean(true);

        private Thread workerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                isRunning.set(true);
                isStopped.set(false);

                logger.logVerbose("%s: logger thread started", logPrefix);

                synchronized (isRunning) {
                    isRunning.notify();
                }

                while (isRunning.get() || !queue.isEmpty()) {
                    if (isStopped.get()) {
                        break;
                    }

                    Record record;
                    try {
                        record = queue.take();
                    } catch (InterruptedException e) {
                        logger.logVerbose("%s: logger thread interrupted, %d remaining", logPrefix, queue.size());
                        isRunning.set(false);
                        continue;
                    }
                    doLog(record.timestamp, record.level, record.message, record.messageArguments);
                }
                isStopped.set(true);
                logger.logVerbose("%s: logger thread stopped, %d remaining", logPrefix, queue.size());
                logger.flush();
            }
        });

        public ThreadedLogWriter() {
            workerThread.start();
            synchronized (isRunning) {
                try {
                    isRunning.wait(50);
                } catch (InterruptedException e) {
                    // dummy
                }
            }
        }

        public void add(long timestamp, LogLevel level, String message, Object... messageArguments) {
            if (!isRunning.get()) {
                logger.logVerbose("{}: logger is not running", logPrefix);
                return;
            }

            Record entry = new Record();
            entry.timestamp = timestamp;
            entry.level = level;
            entry.message = message;
            entry.messageArguments = messageArguments;

            if (!queue.offer(entry)) {
                logger.logVerbose("{}: fail to add log message", logPrefix);
            }
        }
    }

    private final LogLevel maxLevel;

    private final ThreadedLogWriter writer = new ThreadedLogWriter();

    private final String dstRoot;
    private final String dstPrefix;
    private final long dstInterval;

    public FileLogger(String dstRoot, String dstPrefix, long dstInterval) {
        this(dstRoot, dstPrefix, dstInterval, LogLevel.MAX_LEVEL);
    }

    public FileLogger(String dstRoot, String dstPrefix, long dstInterval, LogLevel maxLevel) {
        this.dstRoot = dstRoot;
        this.dstPrefix = dstPrefix;
        this.dstInterval = dstInterval;
        this.maxLevel = maxLevel;
    }

    @Override
    public void flush() {
        while (!writer.queue.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // dummy
            }
        }
    }

    protected String getLogFileBasename(long timestamp, LogLevel level) {
        timestamp -= timestamp % dstInterval;
        String timePart = TimeExtension.toUtcString(timestamp, "%04d%02d%02d%02d%02d%02dZ");
        String logLevelPart = level.ordinal() <= LogLevel.WARNING.ordinal() ? "warning" : "verbose";
        return String.format("%s_%s_%s.%s.log", dstPrefix, timePart, getHostname(), logLevelPart);
    }

    @Override
    public void log(LogLevel level, String message, Object... messageArguments) {
        writer.add(SimpleTime.now().getMillisecondsSinceEpoch(), level, message, messageArguments);
    }

    @Override
    public void close() throws IOException {
        if (!writer.isRunning.get()) {
            return;
        }

        writer.workerThread.interrupt();
        try {
            writer.workerThread.join(2000);
        } catch (InterruptedException e) {
            // dummy
        }
        writer.isStopped.set(true);
    }

    public boolean isStopped() {
        return writer.isStopped.get();
    }

    public void doLog(long timestamp, LogLevel level, String message, Object... messageArguments) {
        if (level.ordinal() > maxLevel.ordinal()) {
            return;
        }
        File dir = new File(dstRoot);
        if (!dir.exists()) {
            dir.mkdirs();
        } else if (!dir.isDirectory()) {
            throw new RuntimeException(String.format("[%s] is not directory", dstRoot));
        }

        File logFile = new File(dstRoot, getLogFileBasename(timestamp, level));

        try (FileWriter w = new FileWriter(logFile, true)) {
            writeLine(w, ",", timestamp, getHostname(), level, message);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
