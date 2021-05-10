package pd.net.serv.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;

import pd.net.serv.RequestContext;

public class HttpRequestContext extends RequestContext {

    private static byte[] readTill(InputStream input, byte[] terms) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        readTill(input, terms, out);
        return out.toByteArray();
    }

    private static void readTill(InputStream input, byte[] terms, ByteArrayOutputStream out) throws IOException {
        assert input != null;
        assert terms != null;
        assert out != null;
        int state = 0;
        while (input.available() > 0) {
            if (state == 0) {
                int ch = input.read();
                if (ch == -1) {
                    throw new IllegalStateException("E: unexpected EOF");
                } else if (ch == terms[0]) {
                    state = 1;
                } else {
                    out.write(ch);
                }
            } else {
                if (state == terms.length) {
                    return;
                }

                int ch = input.read();
                if (ch == -1) {
                    throw new IllegalStateException("E: unexpected EOF");
                } else if (ch == terms[state]) {
                    state = state + 1;
                } else {
                    for (int i = 0; i < state; i++) {
                        out.write(terms[i]);
                    }
                    out.write(ch);
                    state = 0;
                }
            }
        }
        throw new IllegalStateException();
    }

    private static boolean readHeaderKey(InputStream input, ByteArrayOutputStream out) throws IOException {
        assert input != null;
        assert out != null;

        byte HT = '\t';
        byte SP = ' ';
        byte CR = '\r';
        byte LF = '\n';
        byte[] crlf = { '\r', '\n' };

        if (input.available() > 0) {
            int ch = input.read();
            if (ch == SP || ch == HT) {
                out.write(SP);
                readTill(input, crlf, out);
                return false;
            } else if (ch == CR) {
                if (input.available() > 0) {
                    ch = input.read();
                    if (ch == LF) {
                        return false;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            } else {
                out.write(ch);
                readTill(input, new byte[] { ':' }, out);
                return true;
            }
        }
        throw new IllegalStateException();
    }

    public HttpMethod method;
    public String requestUri;
    public int httpMajorVersion;
    public int httpMinorVersion;

    public LinkedList<SimpleEntry<String, String>> requestHeaders;

    public String requestPayload;

    public HttpRequestContext(Socket socket) throws IOException {
        super(socket);
        parseHttpReqMessage();
    }

    /**
     * HttpReqMessage = start-line
     *      *(message-header CRLF)
     *      CRLF
     *      [*(OCTET)]
     * start-line = method SP request-URI SP HTTP-version CRLF
     * HTTP-version = "HTTP" "/" DIGIT "." DIGIT
     * message-header = token ":" *(*field-value-delim field-value)
     * field-value-delim = HT | SP | CRLF (HT | SP)
     * 
     * OCTET = <int value [0,256)>
     * ASCII = <int value [0,128)>
     * CTL = <int value [0,32) and 127>
     * HEX = <regex [0-9a-fA-F]>
     * SEP = <regex [@,;:"/?=] and BACKSLASH and ()[]{}<> and SP and HT>
     * TOKEN_ASCII = <ASCII not CTL or SEP>
     * token = <regex TOKEN_ASCII{1,}>
     */
    private void parseHttpReqMessage() throws IOException {
        byte[] sp = { ' ' };
        byte[] crlf = { '\r', '\n' };

        method = HttpMethod.valueOf(new String(readTill(reqStream, sp)));
        requestUri = new String(readTill(reqStream, sp));
        readTill(reqStream, "HTTP/".getBytes());
        httpMajorVersion = Integer.parseInt(new String(readTill(reqStream, new byte[] { '.' })));
        httpMinorVersion = Integer.parseInt(new String(readTill(reqStream, crlf)));

        requestHeaders = new LinkedList<>();
        parseHeaders(reqStream, requestHeaders);
    }

    private void parseHeaders(InputStream input, LinkedList<SimpleEntry<String, String>> headers) throws IOException {
        byte SP = ' ';
        byte[] crlf = { '\r', '\n' };

        while (true) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean isKey = readHeaderKey(input, out);
            byte[] bytes = out.toByteArray();
            if (bytes.length == 0) {
                break;
            }
            if (isKey) {
                String key = new String(bytes).trim();
                String value = new String(readTill(input, crlf)).trim();
                headers.add(new SimpleEntry<String, String>(key, value));
            } else {
                SimpleEntry<String, String> last = headers.getLast();
                if (last == null) {
                    throw new IllegalStateException();
                }
                last.setValue(last.getValue() + SP + new String(out.toByteArray()).trim());
            }
        }
    }
}
