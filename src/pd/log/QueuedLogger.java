package pd.log;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueuedLogger implements ILogger {

    // message from this logger itself has to be copied to console
    static class CcConsoleLogger implements ILogger {

        private final ILogger logger;

        public CcConsoleLogger(ILogger logger) {
            this.logger = logger;
        }

        @Override
        public void flush() {
            ConsoleLogger.defaultInstance.flush();
            logger.flush();
        }

        @Override
        public void log(long timestamp, LogLevel level, String message, Object... messageArguments) {
            logger.log(level, message, messageArguments);
        }

        public void logCcConsole(String message, Object... messageArguments) {
            ConsoleLogger.defaultInstance.log(LogLevel.INFO, message, messageArguments);
            logger.log(LogLevel.INFO, message, messageArguments);
        }
    }

    class QueuedEntry {
        long timestamp;
        LogLevel level;
        String message;
        Object[] messageArguments;
    }

    private static final String LOG_TAG = QueuedLogger.class.getSimpleName();

    private final CcConsoleLogger logger;

    private final BlockingQueue<QueuedEntry> queue = new LinkedBlockingQueue<QueuedEntry>();

    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(true);

    private Thread workerThread = new Thread(new Runnable() {

        @Override
        public void run() {
            running.set(true);
            stopped.set(false);

            logger.logCcConsole("%s: logger thread started", LOG_TAG);

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
                    logger.logCcConsole("%s: logger thread interrupted, %d remaining", LOG_TAG, queue.size());
                    running.set(false);
                    continue;
                }
                logger.log(entry.timestamp, entry.level, entry.message, entry.messageArguments);
            }
            stopped.set(true);
            logger.logCcConsole("%s: logger thread stopped, %d remaining", LOG_TAG, queue.size());
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

    private void add(long timestamp, LogLevel logLevel, String message, Object... messageArguments) {
        if (!running.get()) {
            logger.logCcConsole("%s: logger is not running", LOG_TAG);
            return;
        }

        QueuedEntry entry = new QueuedEntry();
        entry.timestamp = timestamp;
        entry.level = logLevel;
        entry.message = message;
        entry.messageArguments = messageArguments;

        if (!queue.offer(entry)) {
            logger.logCcConsole("%s: fail to add log message", LOG_TAG);
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

    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public void log(long timestamp, LogLevel level, String message, Object... messageArguments) {
        add(timestamp, level, message, messageArguments);
    }
}
