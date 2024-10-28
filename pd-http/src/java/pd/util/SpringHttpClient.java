package pd.util;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
public class SpringHttpClient {

    private final WebClient client = buildClient();

    public SpringHttpClient() {
    }

    private WebClient buildClient() {
        HttpClient nettyClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(60000, TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(60000, TimeUnit.MILLISECONDS));
                });
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(nettyClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> {
                            configurer.defaultCodecs().maxInMemorySize(-1);
                        })
                        .build())
                .build();
    }

    public Flux<String> fluxGet(String u, Map<String, String> headers) {
        return client.post()
                .uri(u)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            httpHeaders.set(entry.getKey(), entry.getValue());
                        }
                    }
                })
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("SpringHttpClient: fluxGet: error", e);
                    throw new RuntimeException("SpringHttpClient: fluxGet: error", e);
                })
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException e1 = (WebClientResponseException) e;
                        if (e1.getStatusCode().isError()) {
                            return Mono.error(new HttpStatusCodeException(e1.getRawStatusCode(), e1));
                        }
                    }
                    return Mono.error(e);
                });
    }

    public Flux<String> fluxPostJson(String u, Map<String, String> headers, String json) {
        return client.post()
                .uri(u)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            httpHeaders.set(entry.getKey(), entry.getValue());
                        }
                    }
                })
                .body(BodyInserters.fromValue(json))
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException e1 = (WebClientResponseException) e;
                        if (e1.getStatusCode().isError()) {
                            return Mono.error(new HttpStatusCodeException(e1.getRawStatusCode(), e1));
                        }
                    }
                    return Mono.error(e);
                });
    }
}
