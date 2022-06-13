package pd.someserver.demo.httpechoservice.controller;

import pd.someserver.demo.httpechoservice.anno.RouteEndpoint;
import pd.someserver.http.HttpRequestContext;

public class EchoController implements IController {

    @RouteEndpoint(httpMethod = "GET", pathPattern = "/echo")
    public void echo(HttpRequestContext request) {
        System.out.println("haha");
    }
}
