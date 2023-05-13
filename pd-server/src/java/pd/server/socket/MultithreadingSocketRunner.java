package pd.server.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pd.logger.Logger;
import pd.time.SimpleTime;
import pd.time.TimeExtension;

public abstract class MultithreadingSocketRunner extends SocketRunner {

    private static final String logPrefix = MultithreadingSocketRunner.class.getSimpleName();

    private ExecutorService executor;

    public MultithreadingSocketRunner(
            ServerSocket serverSocket,
            int numExecutorThreads,
            Object runningNotifier,
            Logger logger) {
        super(serverSocket, runningNotifier, logger);
        executor = Executors.newFixedThreadPool(numExecutorThreads);
    }

    @Override
    protected void dispatchSocket(Socket socket) {
        executor.execute(() -> {
            long startTime = SimpleTime.now().findMillisecondsSinceEpoch();
            logger.logInfo("{}: run start: {}", socket, TimeExtension.toUtcString(startTime));

            try {
                executeSocket(socket);
            } catch (Exception e) {
                logger.logError("{}: exception when executeSocket: {}: {}", logPrefix, e.getClass().getSimpleName(), e.getMessage());
            } finally {
                closeSocket(socket);
            }

            long endTime = SimpleTime.now().findMillisecondsSinceEpoch();
            logger.logInfo("{}: run end: {}, latency: {}",
                    socket,
                    TimeExtension.toUtcString(endTime),
                    endTime - startTime);
        });
    }

    @Override
    public void stop() {
        super.stop();
        if (executor != null) {
            executor.shutdown();
            executor = null;
            logger.logInfo("{}: executor shutdown", logPrefix);
        }
    }
}
