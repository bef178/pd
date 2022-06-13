package pd.someserver.demo.rpcserver.server;

import java.io.IOException;

import pd.log.LogManager;
import pd.someserver.demo.rpcserver.RpcServer;

public class Main {

    public static void main(String[] args) throws IOException {
        LogManager.useConsoleLogger();
        startRpcServer();
    }

    private static void startRpcServer() throws IOException {
        final int port = 50001;
        RpcServer server = new RpcServer(LogManager.getLogger());
        server.serviceRegistry.register(IEchoService.class, EchoService.class);
        server.serviceRegistry.freeze();
        server.start(port);
    }
}
