package pd.someserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pd.log.ILogger;
import pd.time.Ctime;

public class MultithreadSocketLooper extends SocketLooper {

    private static final String logPrefix = MultithreadSocketLooper.class.getSimpleName();

    private ExecutorService executor;

    public MultithreadSocketLooper(ServerSocket serverSocket, Object runningNotifier, ILogger logger) {
        this(serverSocket, Runtime.getRuntime().availableProcessors(), runningNotifier, logger);
    }

    public MultithreadSocketLooper(ServerSocket serverSocket, int numExecutorThreads,
            Object runningNotifier, ILogger logger) {
        super(serverSocket, runningNotifier, logger);
        executor = Executors.newFixedThreadPool(numExecutorThreads);
    }

    @Override
    protected void dispatchSocket(Socket socket) {

        executor.execute(new Runnable() {

            @Override
            public void run() {
                long startTimestamp = Ctime.now();
                logger.logInfo("{} run start: {}", socket, startTimestamp);

                try {
                    onSocket(socket);
                } catch (Exception e) {
                    logger.logError("{}: exception when onRequest: {}", logPrefix, e.getMessage());
                } finally {
                    closeSocket(socket, logger);
                }

                long endTimestamp = Ctime.now();
                logger.logInfo("{} run end: {}, duration: {}", socket, endTimestamp,
                        endTimestamp - startTimestamp);
            }
        });
    }

    @Override
    public void stop() {
        super.stop();
        if (executor != null) {
            executor.shutdown();
            executor = null;
            logger.logInfo("{} executor shutdown", logPrefix);
        }
    }
}
