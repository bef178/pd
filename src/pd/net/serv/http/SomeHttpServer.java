package pd.net.serv.http;

import java.io.IOException;
import java.net.Socket;

import pd.log.ILogger;
import pd.log.LogManager;
import pd.net.serv.SomeServer;

public class SomeHttpServer extends SomeServer<HttpRequestContext> {

    static final ILogger logger = LogManager.getLogger();

    public SomeHttpServer(int port) {
        super(port);
    }

    @Override
    protected HttpRequestContext buildRequest(Socket socket) throws IOException {
        return new HttpRequestContext(socket);
    }

    @Override
    protected void onRequest(HttpRequestContext request) throws IOException {
        System.out.println("hello world");
        System.out.println(request.method);
        System.out.println(request.requestUri);
        System.out.println(request.httpMajorVersion);
        System.out.println(request.httpMinorVersion);
    }

    public static void main(String[] args) throws IOException {
        new SomeHttpServer(8881).start();
    }
}
