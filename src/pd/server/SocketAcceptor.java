package pd.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketAcceptor {

    static final Logger LOGGER = LoggerFactory.getLogger(SocketAcceptor.class);

    protected static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                LOGGER.error("exception when close socket: {}", e.getMessage());
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
     * runs in server socket thread<br/>
     * should close socket at last<br/>
     * override me<br/>
     */
    protected void onSocket(Socket socket) {
        LOGGER.info("socket handled");
        closeSocket(socket);
    }

    /**
     * should run in server socket thread
     */
    public void start(Object notifier) throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            LOGGER.warn("server socket already at port {}", port);
            return;
        }

        try {
            serverSocket = createServerSocket(port);
            LOGGER.info("server socket started at port {}", port);
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
                    LOGGER.error("exception when onSocket(): {}", e.getMessage());
                }
            }
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        }

        LOGGER.info("server socket stopped");
    }

    /**
     * blocked until server socket thread fully started or failed<br/>
     * threadName null as "ServerSocketThread"<br/>
     */
    public Thread startInNewThread(String threadName) {
        if (threadName == null) {
            threadName = "ServerSocketThread";
        }

        Object notifier = new Object();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                LOGGER.info("runnable starting");
                try {
                    SocketAcceptor.this.start(notifier);
                } catch (IOException e) {
                    LOGGER.error("exception: {}", e.getMessage());
                }
                LOGGER.info("runnable ending");
            }
        });
        thread.setName(String.format("%s-%d", threadName, thread.getId()));

        LOGGER.info("thread [{}] created", thread.getName());

        thread.start();

        synchronized (notifier) {
            try {
                notifier.wait();
            } catch (Exception e) {
                LOGGER.info("notifier exception: {}", e.getMessage());
            }
        }

        return thread;
    }

    /**
     * manually stop
     */
    public void stop() {
        LOGGER.info("stopping");
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                // dummy
            }
        }
    }
}
