package pd.net.serv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import pd.log.ILogger;

public class SocketAcceptorTask implements Runnable {

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

    private volatile AtomicBoolean running = new AtomicBoolean(false);
    private volatile AtomicBoolean stopped = new AtomicBoolean(true);

    public SocketAcceptorTask(ServerSocket serverSocket, Object runningNotifier, ILogger logger) {
        this.serverSocket = serverSocket;
        this.runningNotifier = runningNotifier;
        this.logger = logger;
    }

    public Thread createThread() {
        String threadNamePrefix = getClass().getSimpleName();
        if (threadNamePrefix == null || threadNamePrefix.isEmpty()) {
            // inner class or anonymous class
            threadNamePrefix = SocketAcceptorTask.class.getSimpleName();
        }
        return createThread(threadNamePrefix);
    }

    public Thread createThread(String threadNamePrefix) {
        Thread thread = new Thread(this);
        thread.setName(threadNamePrefix + "-" + thread.getId());
        return thread;
    }

    public boolean isStopped() {
        return stopped.get();
    }

    /**
     * run in server socket thread<br/>
     * should close socket in this method, thus enable handling socket in other threads<br/>
     */
    protected void onSocket(Socket socket) {
        logger.logTrace("onSocket");
        closeSocket(socket, logger);
    }

    @Override
    public void run() {
        running.set(true);
        stopped.set(false);

        if (runningNotifier != null) {
            synchronized (runningNotifier) {
                runningNotifier.notify();
            }
        }

        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try {
            while (running.get()) {
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
                    onSocket(socket);
                } catch (Exception e) {
                    logger.logError("E: exception when onSocket(): {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.logError("E: exception: {}", e.getMessage());
        }

        stopped.set(true);
        logger.logInfo("server socket stopped");
    }

    public void stop() {
        logger.logTrace("stop requested");
        running.set(true);
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
