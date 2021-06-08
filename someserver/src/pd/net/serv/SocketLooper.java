package pd.net.serv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import pd.log.ILogger;

public class SocketLooper implements Runnable {

    private static final String logPrefix = SocketLooper.class.getSimpleName();

    public static void closeSocket(Socket socket, ILogger logger) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                if (logger != null) {
                    logger.logError("E: exception when close socket: {}", e.getMessage());
                }
            }
        }
    }

    private ServerSocket serverSocket;

    private Object runningNotifier;

    protected ILogger logger;

    private volatile AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile AtomicBoolean isStopped = new AtomicBoolean(true);

    public SocketLooper(ServerSocket serverSocket, Object runningNotifier, ILogger logger) {
        this.serverSocket = serverSocket;
        this.runningNotifier = runningNotifier;
        this.logger = logger;
    }

    public Thread createThread() {
        String prefix = getClass().getSimpleName();
        if (prefix == null || prefix.isEmpty()) {
            // anonymous class or inner class
            prefix = getClass().getSuperclass().getSimpleName();
        }
        if (prefix == null || prefix.isEmpty()) {
            prefix = logPrefix;
        }
        return createThread(prefix);
    }

    public Thread createThread(String threadNamePrefix) {
        Thread thread = new Thread(this);
        thread.setName(threadNamePrefix + "-" + thread.getId());
        return thread;
    }

    /**
     * will run in server socket thread<br/>
     * should close socket in the end<br/>
     */
    protected void dispatchSocket(Socket socket) {
        onSocket(socket);
        closeSocket(socket, logger);
    }

    public boolean isStopped() {
        return isStopped.get();
    }

    protected void onSocket(Socket socket) {
        logger.logVerbose("onSocket");
    }

    @Override
    public void run() {
        isRunning.set(true);
        isStopped.set(false);

        if (runningNotifier != null) {
            synchronized (runningNotifier) {
                logger.logInfo("{} started", logPrefix);
                runningNotifier.notify();
            }
        }

        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try {
            while (isRunning.get()) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException e) {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        break;
                    }
                    // exception on server socket, cannot recover
                    throw e;
                }

                try {
                    dispatchSocket(socket);
                } catch (Exception e) {
                    logger.logError("E: exception when onSocket(): {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.logError("E: exception: {}", e.getMessage());
        }

        isStopped.set(true);
        logger.logInfo("server socket stopped");
    }

    public void stop() {
        logger.logVerbose("stop requested");
        isRunning.set(false);
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // dummy
            }
            serverSocket = null;
        }
    }
}
