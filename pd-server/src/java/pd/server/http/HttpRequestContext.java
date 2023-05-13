package pd.server.http;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import pd.server.RequestContext;

public class HttpRequestContext extends RequestContext {

    public final byte[] rawBytes;

    public final String httpMethod;

    public final String requestPath;

    public final List<Map.Entry<String, String>> requestQuery;

    public final String requestFragment;

    public final String httpVersion;

    public final List<Map.Entry<String, String>> httpHeaders;

    public HttpRequestContext(Socket socket) throws IOException {
        super(socket);

        HttpMessageParser parser = new HttpMessageParser(socket.getInputStream());
        try {
            parser.parseHttpRequestMessageHead();
        } finally {
            rawBytes = parser.getRawBytes();
        }
        httpMethod = parser.httpMethod;
        requestPath = parser.requestPath;
        requestQuery = parser.requestQuery;
        requestFragment = parser.requestFragment;
        httpVersion = parser.httpVersion;
        httpHeaders = parser.httpHeaders;
    }
}
