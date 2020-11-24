package pd.net.serv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoService {

    static final Logger LOGGER = LoggerFactory.getLogger(EchoService.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        EchoService server = new EchoService();
        server.start(8881);
    }

    protected void onRequest(RequestContext request) throws IOException {
        StringBuilder ackBody = new StringBuilder();
        ackBody.append("<html>");
        ackBody.append("<head>");
        ackBody.append("<title>Echo Service</title>");
        ackBody.append("</head>");
        ackBody.append("<body>");
        ackBody.append("<h1>Welcome to Echo Service</h1>");
        ackBody.append("<div style=\"font-family: monospace;\">");

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.reqStream));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) {
                // read to end of request headers
                break;
            }
            ackBody.append(line).append("<br/>");
        }
        // don't close reader stream

        ackBody.append("(EOF)").append("<br/>");
        ackBody.append("</div>");
        ackBody.append("</body>");
        ackBody.append("</html>");

        byte[] ackBytes = ackBody.toString().getBytes("utf-8");

        StringBuilder ackHead = new StringBuilder();
        ackHead.append("HTTP/1.0 200").append("\r\n");
        ackHead.append("Content-Type: text/html").append("\r\n");
        ackHead.append("Content-Length: ").append(ackBytes.length).append("\r\n");
        ackHead.append("Connection: close").append("\r\n");
        ackHead.append("\r\n");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.ackStream));
        writer.write(ackHead.toString());
        writer.write(ackBody.toString());
        writer.flush();
    }

    public void start(int port) throws IOException {
        SomeServer server = new SomeServer(port) {
            @Override
            protected void onRequest(RequestContext request) throws IOException {
                EchoService.this.onRequest(request);
            }
        };
        server.start();
    }
}
