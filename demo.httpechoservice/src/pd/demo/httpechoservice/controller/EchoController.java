package pd.demo.httpechoservice.controller;

import pd.demo.httpechoservice.anno.RouteEndpoint;
import pd.net.http.HttpRequestContext;

public class EchoController implements IController {

    @RouteEndpoint(httpMethod = "GET", pathPattern = "/echo")
    public void echo(HttpRequestContext request) {
        System.out.println("haha");
    }
}
