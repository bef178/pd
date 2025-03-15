package pd.util.http;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import reactor.core.publisher.Flux;

public class HttpMan {

    static InetSocketAddress buildSocketAddress(String socketAddressString) {
        if (socketAddressString == null) {
            return null;
        }
        String[] a = socketAddressString.split(":");
        return new InetSocketAddress(a[0], Integer.parseInt(a[1]));
    }

    private final ApacheHttpClient apacheHttpClient;

    private final SpringHttpClient springHttpClient;

    public HttpMan() {
        this(null);
    }

    public HttpMan(String socks5ProxyAddressString) {
        InetSocketAddress socks5ProxyAddress = buildSocketAddress(socks5ProxyAddressString);
        apacheHttpClient = new ApacheHttpClient(socks5ProxyAddress);
        springHttpClient = new SpringHttpClient(socks5ProxyAddress);
    }

    public URI buildUri(String u, Map<String, String> queryParams) {
        return apacheHttpClient.buildUri(u, queryParams);
    }

    public String getPath(String u) {
        return apacheHttpClient.getPath(u);
    }

    public LinkedHashMap<String, String> getQueryParams(String u) {
        return apacheHttpClient.getQueryParams(u);
    }

    public byte[] httpGetForBinary(String u, Map<String, String> headers) {
        return apacheHttpClient.httpGetForBinary(u, headers);
    }

    public String httpGetForText(String u, Map<String, String> headers) {
        return httpGetForText(u, headers, null);
    }

    public String httpGetForText(String u, Map<String, String> headers, Map<String, Object> trace) {
        return doHttpForTextWithTrace(() -> apacheHttpClient.httpGetForText(u, headers), "GET", u, headers, null, trace);
    }

    public String httpPostTextForText(String u, Map<String, String> headers, String text) {
        return httpPostTextForText(u, headers, text, null);
    }

    public String httpPostTextForText(String u, Map<String, String> headers, String text, Map<String, Object> trace) {
        return doHttpForTextWithTrace(() -> apacheHttpClient.httpPostTextForText(u, headers, text), "POST", u, headers, text, trace);
    }

    public String httpPostJsonForText(String u, Map<String, String> headers, String json) {
        return httpPostJsonForText(u, headers, json, null);
    }

    public String httpPostJsonForText(String u, Map<String, String> headers, String json, Map<String, Object> trace) {
        return doHttpForTextWithTrace(() -> apacheHttpClient.httpPostJsonForText(u, headers, json), "POST", u, headers, json, trace);
    }

    private String doHttpForTextWithTrace(Supplier<String> f, String httpMethod, String u, Map<String, String> headers, String bodyString, Map<String, Object> trace) {
        String responseBodyString = null;
        Instant startTime = Instant.now();
        try {
            responseBodyString = f.get();
            return responseBodyString;
        } finally {
            Instant endTime = Instant.now();
            if (trace != null) {
                Map<String, Object> requestTrace = new LinkedHashMap<>();
                requestTrace.put("method", httpMethod);
                requestTrace.put("url", u);
                requestTrace.put("headers", headers);
                requestTrace.put("bodyString", bodyString);
                trace.put("request", requestTrace);

                Map<String, Object> responseTrace = new LinkedHashMap<>();
                responseTrace.put("bodyString", responseBodyString);
                trace.put("response", responseTrace);

                trace.put("startTime", startTime.toString());
                trace.put("endTime", endTime.toString());
                trace.put("latency", endTime.toEpochMilli() - startTime.toEpochMilli());
            }
        }
    }

    public Flux<String> fluxPostJsonForText(String u, Map<String, String> headers, String json) {
        return fluxPostJsonForText(u, headers, json, null);
    }

    public Flux<String> fluxPostJsonForText(String u, Map<String, String> headers, String json, Map<String, Object> trace) {
        Instant startTime = Instant.now();
        List<Map<String, Object>> responseTraces = trace == null ? null : Collections.synchronizedList(new LinkedList<>());

        try {
            return springHttpClient.fluxPostJson(u, headers, json)
                    .map(a -> {
                        Instant endTime = Instant.now();
                        if (responseTraces != null) {
                            Map<String, Object> responseTrace = new LinkedHashMap<>();
                            responseTrace.put("bodyString", a);
                            responseTrace.put("endTime", endTime);
                            responseTrace.put("latency", endTime.toEpochMilli() - startTime.toEpochMilli());
                            responseTraces.add(responseTrace);
                        }
                        return a;
                    });
        } finally {
            Instant endTime = Instant.now();
            if (trace != null) {
                Map<String, Object> requestTrace = new LinkedHashMap<>();
                requestTrace.put("method", "POST");
                requestTrace.put("url", u);
                requestTrace.put("headers", headers);
                requestTrace.put("bodyString", json);
                trace.put("request", requestTrace);

                trace.put("response", responseTraces);

                trace.put("startTime", startTime.toString());
                trace.put("endTime", endTime.toString());
                trace.put("latency", endTime.toEpochMilli() - startTime.toEpochMilli());
            }
        }
    }
}
