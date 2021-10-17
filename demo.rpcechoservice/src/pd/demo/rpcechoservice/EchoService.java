package pd.demo.rpcechoservice;

public class EchoService implements IEchoService {

    @Override
    public String echo(String message) {
        return message;
    }
}
