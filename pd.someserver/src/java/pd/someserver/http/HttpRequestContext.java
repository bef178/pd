package pd.someserver.http;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import pd.someserver.RequestContext;

public class HttpRequestContext extends RequestContext {

    public final byte[] mRawBytes;

    public final String mHttpMethod;

    public final String mRequestPath;

    public final List<Map.Entry<String, String>> mRequestQuery;

    public final String mRequestFragment;

    public final String mHttpVersion;

    public final List<Map.Entry<String, String>> mHttpHeaders;

    public HttpRequestContext(Socket socket) throws IOException {
        super(socket);

        HttpMessageParser parser = new HttpMessageParser(socket.getInputStream());
        try {
            parser.parseHttpRequestMessageHead();
        } finally {
            mRawBytes = parser.getRawBytes();
        }
        mHttpMethod = parser.httpMethod;
        mRequestPath = parser.requestPath;
        mRequestQuery = parser.requestQuery;
        mRequestFragment = parser.requestFragment;
        mHttpVersion = parser.httpVersion;
        mHttpHeaders = parser.httpHeaders;
    }
}
