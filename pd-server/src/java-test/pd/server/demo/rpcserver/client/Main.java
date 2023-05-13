package pd.server.demo.rpcserver.client;

import pd.server.demo.rpcserver.RpcClientStub;
import pd.server.demo.rpcserver.server.IEchoService;

public class Main {

    public static void main(String[] args) {
        call();
    }

    private static void call() {
        final String HOST = "localhost";
        final int PORT = 50001;
        IEchoService service = RpcClientStub.getRemoteService(HOST, PORT, IEchoService.class);
        String input = "echome";
        String result = service.echo(input);
        System.out.println("input: " + input);
        System.out.println("result: " + result);
        assert input.equals(result);
    }
}
