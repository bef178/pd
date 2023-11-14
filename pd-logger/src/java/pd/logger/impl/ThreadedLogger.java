package pd.logger.impl;

import java.io.Closeable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import pd.logger.Logger;

/**
 * holds a thread writing down records to avoid latency from IO
 */
public abstract class ThreadedLogger implements Closeable, Logger {

    private final String logPrefix = ThreadedLogger.class.getSimpleName();

    private final Logger myLogger = ConsoleLogger.defaultInstance;

    private final LinkedBlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isStopped = new AtomicBoolean(true);

    private final Thread workerThread = new Thread(() -> {
        isRunning.set(true);
        isStopped.set(false);

        myLogger.verbose("%s: logger thread started", logPrefix);

        synchronized (isRunning) {
            isRunning.notify();
        }

        while (isRunning.get() || !queue.isEmpty()) {
            if (isStopped.get()) {
                break;
            }

            LogEntry logEntry;
            try {
                logEntry = queue.take();
            } catch (InterruptedException e) {
                myLogger.verbose("%s: logger thread interrupted, %d remaining", logPrefix, queue.size());
                isRunning.set(false);
                continue;
            }
            doLog(logEntry);
        }
        isStopped.set(true);
        myLogger.verbose("%s: logger thread stopped, %d remaining", logPrefix, queue.size());
        myLogger.flush();
    });

    public ThreadedLogger() {
        workerThread.start();
        synchronized (isRunning) {
            try {
                isRunning.wait(50);
            } catch (InterruptedException e) {
                // dummy
            }
        }
    }

    protected void add(LogEntry logEntry) {
        if (!isRunning.get()) {
            myLogger.verbose("{}: logger is not running", logPrefix);
            return;
        }

        if (!queue.offer(logEntry)) {
            myLogger.verbose("{}: fail to add log message", logPrefix);
        }
    }

    @Override
    public void close() {
        if (!isRunning.get()) {
            return;
        }

        workerThread.interrupt();
        try {
            workerThread.join(2000);
        } catch (InterruptedException e) {
            // dummy
        }
        isStopped.set(true);
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

    abstract protected void doLog(LogEntry logEntry);
}
