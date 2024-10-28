package pd.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Slf4j
public class ApacheHttpClient {

    private final CloseableHttpClient client = buildClient();

    public ApacheHttpClient() {
    }

    private CloseableHttpClient buildClient() {
        return HttpClients.custom()
                .setConnectionManager(buildConnectionManager())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.STRICT)
                        .build())
                .build();
    }

    private PoolingHttpClientConnectionManager buildConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofSeconds(60))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofSeconds(60))
                        .setConnectTimeout(Timeout.ofSeconds(2))
                        .setTimeToLive(TimeValue.ofSeconds(120))
                        .build())
                .build();
    }

    @SneakyThrows
    public URI buildUri(String u, Map<String, String> queryParams) {
        URIBuilder uriBuilder = new URIBuilder(u, StandardCharsets.UTF_8);
        if (queryParams != null && !queryParams.isEmpty()) {
            Map<String, String> params = getQueryParams(u);
            params.putAll(queryParams);
            uriBuilder.clearParameters();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        return uriBuilder.build();
    }

    @SneakyThrows
    public String getPath(String u) {
        return new URIBuilder(u, StandardCharsets.UTF_8)
                .getPath();
    }

    @SneakyThrows
    public LinkedHashMap<String, String> getQueryParams(String u) {
        return new URIBuilder(u, StandardCharsets.UTF_8)
                .getQueryParams().stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    @SneakyThrows
    public String httpGet(String u, Map<String, String> headers) {
        HttpGet request = new HttpGet(u);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return client.execute(request, buildClientContext(), response -> {
            StatusLine statusLine = new StatusLine(response);
            log.info("ApacheHttpClient: httpGet: responseStatusLine: {}", statusLine);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            throwIfError(statusLine, responseBody);
            return responseBody;
        });
    }

    private HttpClientContext buildClientContext() {
        HttpClientContext context = HttpClientContext.create();
        context.setRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(15))
                .setResponseTimeout(Timeout.ofSeconds(15))
                .build());
        return context;
    }

    @SneakyThrows
    public String httpPostText(String u, Map<String, String> headers, String text) {
        HttpPost request = new HttpPost(u);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        request.setEntity(new ByteArrayEntity(text.getBytes(StandardCharsets.UTF_8), ContentType.TEXT_PLAIN));

        return client.execute(request, buildClientContext(), response -> {
            StatusLine statusLine = new StatusLine(response);
            log.info("ApacheHttpClient: httpPostText: responseStatusLine: {}", statusLine);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            throwIfError(statusLine, responseBody);
            return responseBody;
        });
    }

    @SneakyThrows
    public String httpPostJson(String u, Map<String, String> headers, String json) {
        HttpPost request = new HttpPost(u);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        request.setEntity(new ByteArrayEntity(json.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON));

        return client.execute(request, buildClientContext(), response -> {
            StatusLine statusLine = new StatusLine(response);
            log.info("ApacheHttpClient: httpPostJson: responseStatusLine: {}", statusLine);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            throwIfError(statusLine, responseBody);
            return responseBody;
        });
    }

    private void throwIfError(StatusLine statusLine, String message) {
        if (statusLine.isError()) {
            throw new HttpStatusCodeException(statusLine.getStatusCode(), message);
        }
    }
}
