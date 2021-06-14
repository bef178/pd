package pd.app.demo.rpcechoservice;

import java.io.IOException;

import pd.app.demo.rpcechoservice.shared.IEchoService;
import pd.net.serv.rpc.RpcServer;

public class Main {

    public static void main(String[] args) throws IOException {
        startRpcService();
    }

    private static void startRpcService() throws IOException {
        final int port = 50001;
        RpcServer server = new RpcServer();
        server.serviceRegistry.register(IEchoService.class, EchoService.class);
        server.serviceRegistry.freeze();
        server.start(port);
    }
}
