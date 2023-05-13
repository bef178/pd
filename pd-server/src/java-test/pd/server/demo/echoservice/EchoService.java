package pd.server.demo.echoservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import pd.logger.Logger;
import pd.logger.LoggerManager;
import pd.server.RequestContext;
import pd.server.SomeServer;

public class EchoService {

    private static final Logger logger = LoggerManager.singleton().getLogger();

    public static void main(String[] args) throws IOException {
        EchoService service = new EchoService();
        service.start(8881);
    }

    public void start(int port) throws IOException {
        SomeServer<RequestContext> server = new SomeServer<RequestContext>(logger) {

            @Override
            protected RequestContext buildRequest(Socket socket) throws IOException {
                return new RequestContext(socket);
            }

            @Override
            protected void executeRequest(RequestContext request) throws IOException {
                EchoService.this.executeRequest(request);
            }
        };
        server.start(port);
    }

    protected void executeRequest(RequestContext request) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Echo Service</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>Echo Service</h1>");

        sb.append("<div style=\"font-family: monospace;\">");
        sb.append("<div>")
                .append("remote-host: ")
                .append(request.remoteInetAddr.getHostAddress()).append(":")
                .append(request.remotePort).append("</div>");
        sb.append("<div>")
                .append("remote-hostname: ")
                .append(request.remoteInetAddr.getHostName())
                .append("</div>");
        sb.append("</div>");

        sb.append("<br/>");

        sb.append("<div style=\"font-family: monospace;\">");

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.reqStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                // only start line and headers
                break;
            }
            sb.append(line).append("<br/>");
        }
        // don't close reader stream

        sb.append("(CRLF)").append("<br/>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");

        String ackBody = sb.toString();
        byte[] ackBytes = ackBody.getBytes(StandardCharsets.UTF_8);

        String ackHead = "HTTP/1.0 200" + "\r\n"
                + "Content-Type: text/html" + "\r\n"
                + "Content-Length: " + ackBytes.length + "\r\n"
                + "Connection: close" + "\r\n" +
                "\r\n";

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.ackStream));
        writer.write(ackHead);
        writer.write(ackBody);
        writer.flush();
    }
}
