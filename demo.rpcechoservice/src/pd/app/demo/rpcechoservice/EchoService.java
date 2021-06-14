package pd.app.demo.rpcechoservice;

import pd.app.demo.rpcechoservice.shared.IEchoService;

public class EchoService implements IEchoService {

    @Override
    public String echo(String message) {
        return message;
    }
}
