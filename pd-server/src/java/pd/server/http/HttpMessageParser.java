package pd.server.http;

import java.io.InputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.fenc.InstallmentByteBuffer;
import pd.fenc.Int32Provider;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;
import pd.util.AsciiExtension;
import pd.util.Int32ArrayExtension;

import static pd.fenc.Int32Provider.EOF;
import static pd.util.AsciiExtension.CR;
import static pd.util.AsciiExtension.HT;
import static pd.util.AsciiExtension.LF;
import static pd.util.AsciiExtension.SP;

public class HttpMessageParser {

    private static final String ERR_INVALID_HTTP_FORMAT = "Invalid http format";
    private static final String ERR_INVALID_HTTP_REQUEST_PATH = "Invalid http request path";
    private static final String ERR_INVALID_HTTP_REQUEST_QUERY = "Invalid http request query";
    private static final String ERR_INVALID_HTTP_REQUEST_FRAGMENT = "Invalid http request fragment";
    private static final String ERR_INVALID_HTTP_VERSION = "Invalid http version";
    private static final String ERR_INVALID_HTTP_HEADER = "Invalid http header";

    private static final String CRLF = "\r\n";

    private final UnicodeProvider src;

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    // for PU log
    private final InstallmentByteBuffer raw = new InstallmentByteBuffer();

    public String httpMethod;

    public String requestPath;

    public List<Map.Entry<String, String>> requestQuery;

    public String requestFragment;

    public String httpVersion;

    public List<Map.Entry<String, String>> httpHeaders;

    public HttpMessageParser(InputStream input) {
        this(input, 64 * 1024);
    }

    public HttpMessageParser(InputStream inputStream, int capacity) {
        this.src = new UnicodeProvider(Int32Provider.wrap(inputStream)) {
            @Override
            public int next() {
                int ch = super.next();
                if (raw.size() > capacity) {
                    throw new ParsingException("Request too long");
                }
                raw.push((byte) ch);
                return ch;
            }
        };
    }

    public byte[] getRawBytes() {
        return raw.copyBytes();
    }

    /**
     * Not fully compatible with rfc7230; compatible with intuition<br/>
     */
    public void parseHttpRequestMessageHead() {
        httpMethod = pickHttpMethod(src);

        if (!scalarPicker.tryEat(src, ' ')) {
            throw new ParsingException(ERR_INVALID_HTTP_FORMAT);
        }

        requestPath = pickUriPath(src);
        requestQuery = pickUriQuery(src);
        requestFragment = pickUriFragment(src);

        if (!scalarPicker.tryEat(src, ' ')) {
            throw new ParsingException(ERR_INVALID_HTTP_FORMAT);
        }

        httpVersion = pickHttpVersion(src);

        if (!scalarPicker.tryEat(src, CRLF)) {
            throw new ParsingException(ERR_INVALID_HTTP_FORMAT);
        }

        httpHeaders = pickHttpHeaders(src);

        if (!scalarPicker.tryEat(src, CRLF)) {
            throw new ParsingException(ERR_INVALID_HTTP_FORMAT);
        }
    }

    private Map.Entry<String, String> pickHttpHeaderEntry(UnicodeProvider it) {
        StringBuilder sb = new StringBuilder();
        String key = null;
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    // expect key, [a-zA-Z0-9_-]+
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (AsciiExtension.isAlnum(ch) || ch == '_' || ch == '-') {
                        sb.appendCodePoint(ch);
                        state = 1;
                    } else if (ch == CR) {
                        it.back();
                        return null;
                    } else {
                        throw new ParsingException(ERR_INVALID_HTTP_HEADER);
                    }
                    break;
                }
                case 1: {
                    // expect key cont.
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (AsciiExtension.isAlnum(ch) || ch == '_' || ch == '-') {
                        sb.appendCodePoint(ch);
                    } else if (ch == ':') {
                        key = sb.toString();
                        sb.setLength(0);
                        state = 4;
                    } else {
                        throw new ParsingException(ERR_INVALID_HTTP_HEADER);
                    }
                    break;
                }
                case 4: {
                    // expect value, .+
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == SP || ch == HT) {
                        // dummy
                    } else if (ch == CR) {
                        state = 8;
                    } else if (ch != EOF) {
                        sb.appendCodePoint(ch);
                    } else {
                        throw new ParsingException(ERR_INVALID_HTTP_HEADER);
                    }
                    break;
                }
                case 8: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == LF) {
                        String value = sb.toString().trim();
                        return new SimpleImmutableEntry<>(key, value);
                    } else {
                        throw new ParsingException(ERR_INVALID_HTTP_HEADER);
                    }
                }
            }
        }
    }

    private List<Map.Entry<String, String>> pickHttpHeaders(UnicodeProvider it) {
        List<Map.Entry<String, String>> httpHeaders = new LinkedList<>();
        while (true) {
            Map.Entry<String, String> entry = pickHttpHeaderEntry(it);
            if (entry == null) {
                break;
            }
            httpHeaders.add(entry);
        }
        return httpHeaders;
    }

    private String pickHttpMethod(UnicodeProvider it) {
        return scalarPicker.pickString(it, ' ');
    }

    private String pickHttpVersion(UnicodeProvider it) {
        if (!scalarPicker.tryEat(it, "HTTP/")) {
            throw new ParsingException(ERR_INVALID_HTTP_VERSION);
        }

        StringBuilder sb = new StringBuilder().append("HTTP/");
        int ch = it.hasNext() ? it.next() : EOF;
        if (!AsciiExtension.isDigit(ch)) {
            throw new ParsingException(ERR_INVALID_HTTP_VERSION);
        }
        sb.appendCodePoint(ch);
        if (!scalarPicker.tryEat(it, '.')) {
            throw new ParsingException(ERR_INVALID_HTTP_VERSION);
        }
        sb.append('.');
        ch = it.hasNext() ? it.next() : EOF;
        if (!AsciiExtension.isDigit(ch)) {
            throw new ParsingException(ERR_INVALID_HTTP_VERSION);
        }
        sb.appendCodePoint(ch);
        return sb.toString();
    }

    /**
     * "#fragment" => "fragment"<br/>
     * "#" => "" <br/>
     * EOF => null <br/>
     */
    private String pickUriFragment(UnicodeProvider it) {
        if (!scalarPicker.tryEat(it, '#')) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = it.hasNext() ? it.next() : EOF;
            if (ch == EOF || ch == ' ') {
                if (ch != EOF) {
                    it.back();
                }
                break;
            } else if (!AsciiExtension.isVisible(ch)) {
                throw new ParsingException(ERR_INVALID_HTTP_REQUEST_FRAGMENT);
            } else {
                sb.appendCodePoint(ch);
            }
        }
        return sb.toString();
    }

    /**
     * stop in front of any terminator
     */
    private String pickUriPath(UnicodeProvider it) {
        int[] terminators = new int[] { '?', '#', ' ' };
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = it.hasNext() ? it.next() : EOF;
            if (Int32ArrayExtension.contains(terminators, ch)) {
                if (ch != EOF) {
                    it.back();
                }
                break;
            } else if (!AsciiExtension.isVisible(ch)) {
                throw new ParsingException(ERR_INVALID_HTTP_REQUEST_PATH);
            } else {
                sb.appendCodePoint(ch);
            }
        }
        return sb.toString();
    }

    /**
     * "" => null<br/>
     * "?" => empty<br/>
     */
    private List<Map.Entry<String, String>> pickUriQuery(UnicodeProvider it) {
        if (!scalarPicker.tryEat(it, '?')) {
            return null;
        }
        List<Map.Entry<String, String>> query = new LinkedList<>();
        while (true) {
            Map.Entry<String, String> entry = pickUriQueryEntry(it);
            if (entry == null) {
                break;
            }
            query.add(entry);
        }
        return query;
    }

    /**
     * stop in front of any of terminators<br/>
     * charset: visible ascii<br/>
     * a&a=&a=a
     */
    private Map.Entry<String, String> pickUriQueryEntry(UnicodeProvider it) {
        int[] terminators = new int[] { '#', ' ' };
        StringBuilder sb = new StringBuilder();
        String key = null;
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    // expect key
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (Int32ArrayExtension.contains(terminators, ch)) {
                        if (ch != EOF) {
                            it.back();
                        }
                        return null;
                    } else if (!AsciiExtension.isVisible(ch)) {
                        throw new ParsingException(ERR_INVALID_HTTP_REQUEST_QUERY);
                    } else if (ch == '=' || ch == '&') {
                        // empty key
                        throw new ParsingException(ERR_INVALID_HTTP_REQUEST_QUERY);
                    } else {
                        sb.appendCodePoint(ch);
                        state = 1;
                    }
                    break;
                }
                case 1: {
                    // expect key cont.
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (Int32ArrayExtension.contains(terminators, ch)) {
                        if (ch != EOF) {
                            it.back();
                        }
                        key = sb.toString();
                        return new SimpleImmutableEntry<>(key, "");
                    } else if (!AsciiExtension.isVisible(ch)) {
                        throw new ParsingException(ERR_INVALID_HTTP_REQUEST_QUERY);
                    } else if (ch == '=') {
                        key = sb.toString();
                        sb.setLength(0);
                        state = 4;
                    } else if (ch == '&') {
                        key = sb.toString();
                        return new SimpleImmutableEntry<>(key, "");
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case 4: {
                    // expect value
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (Int32ArrayExtension.contains(terminators, ch)) {
                        if (ch != EOF) {
                            it.back();
                        }
                        return new SimpleImmutableEntry<>(key, "");
                    } else if (!AsciiExtension.isVisible(ch)) {
                        throw new ParsingException(ERR_INVALID_HTTP_REQUEST_QUERY);
                    } else if (ch == '&') {
                        return new SimpleImmutableEntry<>(key, "");
                    } else {
                        sb.appendCodePoint(ch);
                        state = 5;
                    }
                    break;
                }
                case 5: {
                    // expect value cont.
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (Int32ArrayExtension.contains(terminators, ch)) {
                        if (ch != EOF) {
                            it.back();
                        }
                        return new SimpleImmutableEntry<>(key, sb.toString());
                    } else if (!AsciiExtension.isVisible(ch)) {
                        throw new ParsingException(ERR_INVALID_HTTP_REQUEST_QUERY);
                    } else if (ch == '&') {
                        return new SimpleImmutableEntry<>(key, sb.toString());
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
            }
        }
    }
}
