package pd.server.demo.rpcserver.server;

public class EchoService implements IEchoService {

    @Override
    public String echo(String message) {
        return message;
    }
}
