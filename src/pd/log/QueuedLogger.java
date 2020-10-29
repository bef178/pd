package pd.log;

import static pd.log.LogManager.Util.isAcceptable;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueuedLogger implements ILogger {

    // message from this logger itself has to be copied to console
    static class CcConsoleLogger implements ILogger {

        private final ILogger consoleLogger;
        private final ILogger logger;

        public CcConsoleLogger(ILogger logger) {
            this.consoleLogger = ConsoleLogger.defaultInstance;
            this.logger = logger;
        }

        @Override
        public void flush() {
            consoleLogger.flush();
            logger.flush();
        }

        @Override
        public void log(long timestamp, LogLevel level, String message) {
            logger.log(timestamp, level, message);
        }

        public void logInfoCcConsole(String message, Object... messageArguments) {
            consoleLogger.logInfo(message, messageArguments);
            logger.logInfo(message, messageArguments);
        }

        @Override
        public LogLevel getMaxAcceptableLogLevel() {
            return logger == null ? null : logger.getMaxAcceptableLogLevel();
        }
    }

    class QueuedEntry {
        long timestamp;
        LogLevel level;
        String message;
    }

    private static final String logTag = QueuedLogger.class.getSimpleName();

    private final CcConsoleLogger logger;

    private final BlockingQueue<QueuedEntry> queue = new LinkedBlockingQueue<QueuedEntry>();

    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(true);

    private Thread workerThread = new Thread(new Runnable() {

        @Override
        public void run() {
            running.set(true);
            stopped.set(false);

            logger.logInfoCcConsole("%s: logger thread started", logTag);

            synchronized (running) {
                running.notify();
            }

            while (running.get() || !queue.isEmpty()) {
                if (stopped.get()) {
                    break;
                }

                QueuedEntry entry;
                try {
                    entry = queue.take();
                } catch (InterruptedException e) {
                    logger.logInfoCcConsole("%s: logger thread interrupted, %d remaining", logTag, queue.size());
                    running.set(false);
                    continue;
                }
                logger.log(entry.timestamp, entry.level, entry.message);
            }
            stopped.set(true);
            logger.logInfoCcConsole("%s: logger thread stopped, %d remaining", logTag, queue.size());
            logger.flush();
        }
    });

    public QueuedLogger(ILogger logger) {
        this.logger = new CcConsoleLogger(logger);
        workerThread.start();

        synchronized (running) {
            try {
                running.wait(50);
            } catch (InterruptedException e) {
                // dummy
            }
        }
    }

    private void add(long timestamp, LogLevel logLevel, String message) {
        if (!running.get()) {
            logger.logInfoCcConsole("%s: logger is not running", logTag);
            return;
        }

        QueuedEntry entry = new QueuedEntry();
        entry.timestamp = timestamp;
        entry.level = logLevel;
        entry.message = message;

        if (!queue.offer(entry)) {
            logger.logInfoCcConsole("%s: fail to add log message", logTag);
        }
    }

    @Override
    public void close() throws IOException {
        if (!running.get()) {
            return;
        }

        workerThread.interrupt();
        try {
            workerThread.join(2000);
        } catch (InterruptedException e) {
            // dummy
        }
        stopped.set(true);
    }

    @Override
    public void flush() {
        while (!queue.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // dummy
            }
        }
    }

    @Override
    public LogLevel getMaxAcceptableLogLevel() {
        return logger == null ? null : logger.getMaxAcceptableLogLevel();
    }

    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public void log(long timestamp, LogLevel level, String message) {
        if (!isAcceptable(level, getMaxAcceptableLogLevel())) {
            return;
        }
        add(timestamp, level, message);
    }
}
