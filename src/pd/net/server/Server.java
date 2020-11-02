package pd.net.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pd.log.ILogger;
import pd.log.LogManager;

public class Server extends SocketAcceptor {

    static final ILogger logger = LogManager.getLogger();

    private ExecutorService executor;

    private final int numThreads;

    public Server(int port) {
        this(port, 0);
    }

    public Server(int port, int numThreads) {
        super(port);
        if (numThreads <= 0) {
            numThreads = Runtime.getRuntime().availableProcessors() + 1;
        }
        this.numThreads = numThreads;
    }

    /**
     * unnecessary to close socket<br/>
     * override me<br/>
     */
    protected void onRequest(RequestContext request) throws IOException {
        logger.logTrace("onRequest");
    }

    @Override
    protected final void onSocket(Socket socket) {

        executor.execute(new Runnable() {

            @Override
            public void run() {
                RequestContext request = null;
                try {
                    request = new RequestContext(socket);
                } catch (IOException e) {
                    logger.logError("E: exception when make request: {}", e.getMessage());
                }

                try {
                    onRequest(request);
                } catch (Exception e) {
                    logger.logError("E: exception when handle request: {}", e.getMessage());
                } finally {
                    closeSocket(socket);
                }
            }
        });

    }

    @Override
    public void doStart(Object notifier) throws IOException {
        if (executor != null && !executor.isShutdown()) {
            logger.logError("E: executor already running");
            return;
        }

        executor = Executors.newFixedThreadPool(numThreads);
        logger.logInfo("executor created with {} threads", numThreads);

        try {
            super.doStart(notifier);
        } finally {
            if (executor != null) {
                executor.shutdown();
                executor = null;
                logger.logInfo("executor shutdown");
            }
        }
    }
}
