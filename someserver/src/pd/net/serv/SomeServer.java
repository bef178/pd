package pd.net.serv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import pd.log.ILogger;
import pd.log.LogManager;

public abstract class SomeServer<T extends RequestContext> {

    static final ILogger logger = LogManager.getLogger();

    public static ServerSocket createServerSocket(final int port, int numAttempts, int retryInterval, ILogger logger)
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
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e1) {
                    // dummy
                }
            }
        }

        // should not reach here
        throw new IllegalStateException();
    }

    private final int port;

    private int numExecutorThreads;

    private SocketLooper acceptorLooper;

    private Thread acceptorThread;

    public SomeServer(int port) {
        this(port, Runtime.getRuntime().availableProcessors() + 1);
    }

    public SomeServer(int port, int numThreads) {
        this.port = port;
        this.numExecutorThreads = numThreads;
    }

    protected abstract T buildRequest(Socket socket) throws IOException;

    protected abstract void onRequest(T request) throws IOException;

    /**
     * create a server socket and run in a new thread<br/>
     * blocked until the new thread is fully started<br/>
     */
    public void start() throws IOException {
        startServerSocketThread();
    }

    private void startServerSocketThread() throws IOException {
        if (acceptorLooper != null && !acceptorLooper.isStopped()) {
            logger.logError("E: socketAcceptor not stopped");
            return;
        }

        if (acceptorThread != null && acceptorThread.isAlive()) {
            logger.logError("E: ServerSocketAcceptorThread is already running");
            return;
        }

        ServerSocket serverSocket = createServerSocket(port, 2, 1000, logger);
        logger.logInfo("server socket created");

        Object notifier = new Object();

        acceptorLooper = new MultithreadSocketLooper(serverSocket, numExecutorThreads, notifier, logger) {

            @Override
            protected final void onSocket(Socket socket) {
                T request = null;
                try {
                    request = buildRequest(socket);
                } catch (IOException e) {
                    logger.logError("E: exception when buildRequest: {}", e.getMessage());
                    return;
                }

                try {
                    onRequest(request);
                } catch (Exception e) {
                    logger.logError("E: exception when onRequest: {}", e.getMessage());
                }
            }
        };

        acceptorThread = acceptorLooper.createThread();
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
        if (acceptorLooper != null) {
            acceptorLooper.stop();
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
