package pd.net.serv;

import static pd.net.serv.AcceptSocketLooper.closeSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pd.log.ILogger;
import pd.log.LogManager;
import pd.time.Ctime;

public abstract class SomeServer<T extends RequestContext> {

    static final ILogger logger = LogManager.getLogger();

    public static ServerSocket createServerSocket(final int port, int numAttempts, int interval, ILogger logger)
            throws IOException {
        for (int i = 0; i < numAttempts; i++) {
            try {
                return new ServerSocket(port);
            } catch (IOException e) {
                if (i == numAttempts) {
                    throw e;
                }

                if (logger != null) {
                    logger.logWarning("W: create socket fails, IOException: {}", e.getMessage());
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e1) {
                    // dummy
                }
            }
        }

        // should not reach here
        throw new IllegalStateException();
    }

    private final int port;

    private ExecutorService executor;

    private int numExecutorThreads;

    private AcceptSocketLooper acceptorTask;

    private Thread acceptorThread;

    public SomeServer(int port) {
        this(port, Runtime.getRuntime().availableProcessors() + 1);
    }

    public SomeServer(int port, int numThreads) {
        this.port = port;
        this.numExecutorThreads = numThreads;
    }

    protected abstract T buildRequest(Socket socket) throws IOException;

    /**
     * unnecessary to close socket<br/>
     */
    protected abstract void onRequest(T request) throws IOException;

    private void onSocket(Socket socket) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                long startTimestamp = Ctime.now();
                logger.logInfo("{} runnable start: {}", socket, startTimestamp);

                T request = null;
                try {
                    request = buildRequest(socket);
                } catch (IOException e) {
                    logger.logError("E: exception when make request: {}", e.getMessage());
                    closeSocket(socket, logger);
                    return;
                }

                try {
                    onRequest(request);
                } catch (Exception e) {
                    logger.logError("E: exception when onRequest: {}", e.getMessage());
                } finally {
                    closeSocket(socket, logger);
                }

                long endTimestamp = Ctime.now();
                logger.logInfo("{} runnable end: {}, duration: {}", socket, endTimestamp,
                        endTimestamp - startTimestamp);
            }
        });
    }

    /**
     * create a server socket and run in a new thread<br/>
     * blocked until server socket thread fully started<br/>
     */
    public void start() throws IOException {
        if (executor != null && !executor.isShutdown()) {
            logger.logError("E: executor already running");
            return;
        }

        if (acceptorTask != null && !acceptorTask.isStopped()) {
            logger.logError("E: socketAcceptor not stopped");
            return;
        }

        if (acceptorThread != null && acceptorThread.isAlive()) {
            logger.logError("E: ServerSocketAcceptorThread is already running");
            return;
        }

        executor = Executors.newFixedThreadPool(numExecutorThreads);
        logger.logInfo("executor created with {} threads", numExecutorThreads);

        ServerSocket serverSocket = createServerSocket(port, 2, 1000, logger);
        logger.logInfo("server socket created", numExecutorThreads);

        Object notifier = new Object();

        acceptorTask = new AcceptSocketLooper(serverSocket, notifier, logger) {
            @Override
            protected final void onSocket(Socket socket) {
                SomeServer.this.onSocket(socket);
            }
        };

        acceptorThread = acceptorTask.createThread();
        logger.logInfo("ServerSocketAcceptorThread [{}] created", acceptorThread.getName());

        acceptorThread.start();
        synchronized (notifier) {
            try {
                notifier.wait();
            } catch (Exception e) {
                logger.logError("E: notifier exception: {}", e.getMessage());
            }
        }
        logger.logInfo("ServerSocketAcceptorThread [{}] fully started", acceptorThread.getName());
    }

    public void stop() {
        if (acceptorTask != null) {
            acceptorTask.stop();
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
            logger.logInfo("executor shutdown");
        }
        try {
            acceptorThread.join(4000);
        } catch (InterruptedException e) {
            // dummy
        } finally {
            acceptorThread = null;
        }
    }
}
