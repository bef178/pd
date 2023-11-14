package pd.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import pd.logger.Logger;

public abstract class SocketRunner implements Runnable {

    private static final String logPrefix = SocketRunner.class.getSimpleName();

    private ServerSocket serverSocket;

    private final Object runningNotifier;

    protected Logger logger;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isStopped = new AtomicBoolean(true);

    public SocketRunner(ServerSocket serverSocket, Object runningNotifier, Logger logger) {
        this.serverSocket = serverSocket;
        this.runningNotifier = runningNotifier;
        this.logger = logger;
    }

    public String findName() {
        String namePrefix = getClass().getSimpleName();
        if (namePrefix.isEmpty()) {
            // anonymous class or inner class
            namePrefix = getClass().getSuperclass().getSimpleName();
            if (namePrefix.isEmpty()) {
                namePrefix = logPrefix;
            }
        }
        return namePrefix;
    }

    /**
     * will run in server socket thread<br/>
     * should close socket in the end<br/>
     */
    protected void dispatchSocket(Socket socket) {
        executeSocket(socket);
        closeSocket(socket);
    }

    protected void executeSocket(Socket socket) {
        logger.info("executing socket;" + Thread.currentThread().getId());
    }

    protected void closeSocket(Socket socket) {
        logger.info("closing socket;" + Thread.currentThread().getId());
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                logger.error("E: exception when close socket: {}", e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        isRunning.set(true);
        isStopped.set(false);

        if (runningNotifier != null) {
            synchronized (runningNotifier) {
                logger.info("{} started", logPrefix);
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
                    logger.error("E: exception when onSocket(): {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("E: exception: {}", e.getMessage());
        }

        isStopped.set(true);
        logger.info("server socket stopped");
    }

    public void stop() {
        logger.verbose("stop requested");
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

    public boolean isStopped() {
        return isStopped.get();
    }
}
