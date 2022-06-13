package pd.someserver.demo.rpcserver.server;

public class EchoService implements IEchoService {

    @Override
    public String echo(String message) {
        return message;
    }
}
