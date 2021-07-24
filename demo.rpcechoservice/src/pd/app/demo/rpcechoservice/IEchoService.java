package pd.app.demo.rpcechoservice;

/**
 * used by both server and client
 */
public interface IEchoService {

    public String echo(String message);
}
