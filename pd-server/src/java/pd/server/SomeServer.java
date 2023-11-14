package pd.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import pd.logger.Logger;
import pd.server.socket.MultithreadingSocketRunner;

public abstract class SomeServer<T> {

    public static ServerSocket createServerSocket(final int port, Logger logger) throws IOException {
        final int numAttempts = 2;
        final int retryInterval = 1000;
        for (int i = 0; i < numAttempts; i++) {
            try {
                return new ServerSocket(port);
            } catch (IOException e) {
                if (i == numAttempts - 1) {
                    throw e;
                }

                if (logger != null) {
                    logger.warning("W: create socket fails, IOException: {}", e.getMessage());
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

    protected final Logger logger;

    private final int numExecutorThreads;

    private MultithreadingSocketRunner acceptorRunner;

    private Thread acceptorThread;

    public SomeServer(Logger logger) {
        this(Runtime.getRuntime().availableProcessors() + 1, logger);
    }

    public SomeServer(int numThreads, Logger logger) {
        this.logger = logger;
        this.numExecutorThreads = numThreads;
    }

    /**
     * create a server socket and run in a new thread<br/>
     * blocked until the new thread is fully started<br/>
     */
    public void start(int port) throws IOException {
        startServerSocketThread(port);
    }

    private void startServerSocketThread(int port) throws IOException {
        if (acceptorRunner != null && !acceptorRunner.isStopped()) {
            logger.error("E: socketAcceptor not stopped");
            return;
        }

        if (acceptorThread != null && acceptorThread.isAlive()) {
            logger.error("E: ServerSocketAcceptorThread is already running");
            return;
        }

        ServerSocket serverSocket = createServerSocket(port, logger);
        logger.info("server socket created");

        Object notifier = new Object();

        acceptorRunner = new MultithreadingSocketRunner(serverSocket, numExecutorThreads, notifier, logger) {

            @Override
            protected void executeSocket(Socket socket) {
                super.executeSocket(socket);
                T request;
                try {
                    request = buildRequest(socket);
                } catch (IOException e) {
                    logger.error("E: exception when buildRequest: {}", e.getMessage());
                    return;
                }

                try {
                    executeRequest(request);
                } catch (Exception e) {
                    logger.error("E: exception when executeRequest: {}: {}", e.getClass().getSimpleName(), e.getMessage());
                }
            }
        };

        Thread thread = new Thread(acceptorRunner);
        thread.setName(acceptorRunner.findName() + "-" + thread.getId());
        acceptorThread = thread;
        logger.info("ServerSocketAcceptorThread [{}] created", acceptorThread.getName());

        acceptorThread.start();
        synchronized (notifier) {
            try {
                notifier.wait();
            } catch (Exception e) {
                logger.error("E: notifier exception: {}", e.getMessage());
            }
        }
        logger.info("ServerSocketAcceptorThread [{}] fully started", acceptorThread.getName());
    }

    protected abstract T buildRequest(Socket socket) throws IOException;

    protected abstract void executeRequest(T request) throws IOException;

    public void stop() {
        if (acceptorRunner != null) {
            acceptorRunner.stop();
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
