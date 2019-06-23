package pd.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends SocketAcceptor {

    static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

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
        LOGGER.info("request handled");
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
                    LOGGER.error("exception when make request: {}", e.getMessage());
                }

                try {
                    onRequest(request);
                } catch (Exception e) {
                    LOGGER.error("exception when handle request: {}", e.getMessage());
                } finally {
                    closeSocket(socket);
                }
            }
        });

    }

    @Override
    public void start(Object notifier) throws IOException {
        if (executor != null && !executor.isShutdown()) {
            LOGGER.error("executor already running");
            return;
        }

        executor = Executors.newFixedThreadPool(numThreads);
        LOGGER.info("executor created with {} threads", numThreads);

        try {
            super.start(notifier);
        } finally {
            if (executor != null) {
                executor.shutdown();
                executor = null;
                LOGGER.info("executor shutdown");
            }
        }
    }
}
