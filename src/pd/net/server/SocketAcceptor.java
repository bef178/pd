package pd.net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import pd.log.ILogger;
import pd.log.LogManager;

public class SocketAcceptor {

    static final ILogger logger = LogManager.getLogger();

    protected static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                logger.logError("E: exception when close socket: {}", e.getMessage());
            }
        }
    }

    private static ServerSocket createServerSocket(final int port) throws IOException {
        int numRetry = 1;
        int retryInterval = 1000;
        while (true) {
            try {
                return new ServerSocket(port);
            } catch (IOException e) {
                if (numRetry-- <= 0) {
                    throw e;
                }
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e1) {
                    // dummy
                }
            }
        }
    }

    private final int port;

    private ServerSocket serverSocket = null;

    public SocketAcceptor(int port) {
        this.port = port;
    }

    /**
     * run in server socket thread<br/>
     */
    public void doStart(Object notifier) throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            logger.logWarning("server socket already started at port {}", port);
            return;
        }

        try {
            serverSocket = createServerSocket(port);
            logger.logInfo("server socket started at port {}", port);
        } catch (Exception e) {
            serverSocket = null;
            throw e;
        } finally {
            // "finally" then "throw"
            if (notifier != null) {
                synchronized (notifier) {
                    notifier.notify();
                }
            }
        }

        try {
            while (true) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException e) {
                    if (serverSocket.isClosed()) {
                        break;
                    }
                    // exception on server socket, cannot recover
                    throw e;
                }

                try {
                    onSocket(socket);
                } catch (Exception e) {
                    logger.logError("E: exception when onSocket(): {}", e.getMessage());
                }
            }
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        }

        logger.logInfo("server socket stopped");
    }

    /**
     * run in server socket thread<br/>
     * should close socket at last<br/>
     */
    protected void onSocket(Socket socket) {
        logger.logTrace("onSocket");
        closeSocket(socket);
    }

    /**
     * create a thread to start server socket<br/>
     * blocked until server socket thread fully started or failed<br/>
     */
    public Thread start(String threadName) {
        if (threadName == null) {
            threadName = SocketAcceptor.class.getSimpleName() + ".ServerSocketThread";
        }

        Object notifier = new Object();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                logger.logTrace("runnable starting");
                try {
                    SocketAcceptor.this.doStart(notifier);
                } catch (IOException e) {
                    logger.logError("E: exception: {}", e.getMessage());
                }
                logger.logTrace("runnable ending");
            }
        });
        thread.setName(threadName + "-" + thread.getId());

        logger.logInfo("thread [{}] created", thread.getName());

        thread.start();

        synchronized (notifier) {
            try {
                notifier.wait();
            } catch (Exception e) {
                logger.logError("E: notifier exception: {}", e.getMessage());
            }
        }

        return thread;
    }

    public void stop() {
        logger.logInfo("stopping");
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                // dummy
            }
        }
    }
}
