package pd.app.echoservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import pd.log.ILogger;
import pd.log.LogManager;
import pd.net.serv.RequestContext;
import pd.net.serv.SomeServer;

public class EchoService {

    static final ILogger LOGGER = LogManager.getLogger();

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
        ackBody.append("<h1>Echo Service</h1>");

        ackBody.append("<div style=\"font-family: monospace;\">");
        ackBody.append("<div>")
                .append("remote-host: ")
                .append(request.remoteInetAddr.getHostAddress()).append(":")
                .append(request.remotePort).append("</div>");
        ackBody.append("<div>")
                .append("remote-hostname: ")
                .append(request.remoteInetAddr.getHostName())
                .append("</div>");
        ackBody.append("</div>");

        ackBody.append("<br/>");

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

        SomeServer<RequestContext> server = new SomeServer<RequestContext>(port) {

            @Override
            protected RequestContext buildRequest(Socket socket) throws IOException {
                return new RequestContext(socket);
            }

            @Override
            protected void onRequest(RequestContext request) throws IOException {
                EchoService.this.onRequest(request);
            }
        };
        server.start();
    }
}
