package pd.someserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RequestContext implements Closeable {

    private final Socket socket;

    public final InetAddress remoteInetAddr;

    public final int remotePort;

    public final InputStream reqStream;

    public final OutputStream ackStream;

    public RequestContext(Socket socket) throws IOException {
        if (socket == null) {
            throw new NullPointerException();
        }

        this.socket = socket;

        this.remoteInetAddr = socket.getInetAddress();
        this.remotePort = socket.getPort();

        this.reqStream = socket.getInputStream();
        this.ackStream = socket.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        if (ackStream != null) {
            ackStream.close();
        }
        if (reqStream != null) {
            reqStream.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}
