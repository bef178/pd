package pd.app.demo.rpcechoservice;

import java.io.IOException;

import pd.app.demo.rpcechoservice.shared.IEchoService;
import pd.log.LogManager;
import pd.net.serv.rpc.RpcServer;

public class Main {

    public static void main(String[] args) throws IOException {
        LogManager.useConsoleLogger();
        startRpcService();
    }

    private static void startRpcService() throws IOException {
        final int port = 50001;
        RpcServer server = new RpcServer(LogManager.getLogger());
        server.serviceRegistry.register(IEchoService.class, EchoService.class);
        server.serviceRegistry.freeze();
        server.start(port);
    }
}
