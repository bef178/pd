package blackbox.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pd.net.serv.rpc.RpcClientStub;
import pd.net.serv.rpc.RpcServer;

public class Test_RpcServer {

    public static final String HOST = "localhost";

    public static final int PORT = 50001;

    private RpcServer server;

    @AfterEach
    public void cleanup() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void clientCall() {
        IEchoService service = RpcClientStub.getRemoteService(HOST, PORT, IEchoService.class);
        String input = "echome";
        String result = service.echo(input);
        assertEquals(input, result);
    }

    @BeforeEach
    public void startup() throws IOException {
        server = new RpcServer(PORT);
        server.serviceRegistry.register(IEchoService.class, EchoService.class);
        server.serviceRegistry.freeze();
        server.start();
    }
}
