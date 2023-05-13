package pd.server.demo.httpechoservice.controller;

import java.util.Map;

import pd.server.demo.httpechoservice.annotation.RouteEndpoint;
import pd.server.http.HttpRequestContext;

public class EchoController implements Controller {

    @RouteEndpoint(httpMethod = "GET", pathPattern = "/echo")
    public void echo(HttpRequestContext request, Map<String, String> pathParams) {
        System.out.println("haha");
    }
}
