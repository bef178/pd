package pd.net.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pd.net.server.RequestContext;
import pd.net.server.Server;

public class EchoServer extends Server {

    static final Logger LOGGER = LoggerFactory.getLogger(EchoServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        EchoServer server = new EchoServer(8881);
        server.startInNewThread(null).join();
    }

    public EchoServer(int port) {
        super(port);
    }

    protected void onRequest(RequestContext request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.reqStream));
        PrintWriter writer = new PrintWriter(request.ackStream, true);

        StringBuilder ackBody = new StringBuilder();
        ackBody.append("<html>");
        ackBody.append("<head>");
        ackBody.append("<title>Echo server</title>");
        ackBody.append("</head>");
        ackBody.append("<body>");
        ackBody.append("<h1>Welcome to Echo server</h1>");
        ackBody.append("<div style=\"font-family: monospace;\">");
        while (reader.ready()) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            ackBody.append(line).append("<br/>");
        }
        ackBody.append("(EOF)").append("<br/>");
        ackBody.append("</div>");
        ackBody.append("</body>");
        ackBody.append("</html>");

        StringBuilder ackHead = new StringBuilder();
        ackHead.append("HTTP/1.0 200").append("\r\n");
        ackHead.append("Content-Type: text/html").append("\r\n");
        ackHead.append("Content-Length: ").append(ackBody.length()).append("\r\n");
        ackHead.append("\r\n");

        writer.print(ackHead.toString());
        writer.print(ackBody.toString());
        writer.flush();
    }
}
