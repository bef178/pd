package pd.app.demo.rpcechoservice.client;

import pd.app.demo.rpcechoservice.shared.IEchoService;
import pd.net.serv.rpc.RpcClientStub;

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
