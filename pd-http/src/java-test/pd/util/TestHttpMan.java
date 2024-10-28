package pd.util;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import fun.baidutoken.entity.BaiduTokenResponse;
import fun.ernie.entity.ErnieMessage;
import fun.ernie.entity.ErnieRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pd.jaco.JsonMan;

public class TestHttpMan {

    JsonMan jsonMan = new JsonMan();
    HttpMan httpMan = new HttpMan();

    public void queryErnie() {
        final String serviceUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";

        ErnieRequest request = new ErnieRequest();
        request.messages = new LinkedList<>();
        request.messages.add(new ErnieMessage(ErnieMessage.ROLE_USER, "hello"));
        request.stream = true;

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("access_token", getToken());
        String u = httpMan.buildUri(serviceUrl, queryParams).toString();

        Map<String, Object> trace = new LinkedHashMap<>();

        httpMan.fluxPostJson(u, null, jsonMan.serialize(request), trace)
                .map(a -> {
                    System.out.println(a);
                    return a;
                }).blockLast(Duration.ofSeconds(10));
    }

    private String getToken() {
        final String serviceUrl = "https://aip.baidubce.com/oauth/2.0/token";
        final String clientId = "<clientId>";
        final String clientSecret = "<clientSecret>";

        Map<String, String > queryParams = new LinkedHashMap<>();
        queryParams.put("grant_type", "client_credentials");
        queryParams.put("client_id", clientId);
        queryParams.put("client_secret", clientSecret);
        String u = httpMan.buildUri(serviceUrl, queryParams).toString();

        String responseBodyString = httpMan.httpGet(u, null);
        BaiduTokenResponse response = jsonMan.deserialize(responseBodyString, BaiduTokenResponse.class);
        return response.access_token;
    }

    @Test
    public void testHttpGet() {
        httpMan.httpGet("https://www.baidu.com/", null);
        Assertions.assertThrows(UnknownHostException.class, () -> httpMan.httpGet("https://www.example123123123.com/", null));
    }
}
