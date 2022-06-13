package pd.someserver.demo.rpcserver.server;

/**
 * used by both server and client
 */
public interface IEchoService {

    public String echo(String message);
}
