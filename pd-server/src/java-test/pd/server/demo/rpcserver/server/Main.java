package pd.server.demo.rpcserver.server;

import java.io.IOException;

import pd.logger.LoggerManager;
import pd.server.demo.rpcserver.RpcServer;

public class Main {

    public static void main(String[] args) throws IOException {
        startRpcServer();
    }

    private static void startRpcServer() throws IOException {
        final int port = 50001;
        RpcServer server = new RpcServer(LoggerManager.singleton().getLogger());
        server.serviceRegistry.register(IEchoService.class, EchoService.class);
        server.serviceRegistry.freeze();
        server.start(port);
    }
}
