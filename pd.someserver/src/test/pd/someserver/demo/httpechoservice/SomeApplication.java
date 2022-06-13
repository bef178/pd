package pd.someserver.demo.httpechoservice;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import pd.log.ILogger;
import pd.log.LogManager;
import pd.someserver.SomeServer;
import pd.someserver.demo.httpechoservice.RouteRegistry.RouteResult;
import pd.someserver.demo.httpechoservice.anno.RouteEndpoint;
import pd.someserver.demo.httpechoservice.controller.EchoController;
import pd.someserver.demo.httpechoservice.controller.IController;
import pd.someserver.http.HttpRequestContext;
import pd.util.CurlyBracketPattern;

public class SomeApplication extends SomeServer<HttpRequestContext> {

    public static void main(String[] args) throws IOException, InterruptedException {
        LogManager.useConsoleLogger();
        SomeApplication app = new SomeApplication(LogManager.getLogger());
        app.register(new EchoController());
        app.start(8881);
    }

    public SomeApplication(ILogger logger) {
        super(logger);
    }

    @Override
    protected HttpRequestContext buildRequest(Socket socket) throws IOException {
        return new HttpRequestContext(socket);
    }

    @Override
    protected void onRequest(HttpRequestContext request) {
        RouteResult routeResult = router.route(request);
        if (routeResult != null) {
            try {
                Object result = routeResult.method.invoke(routeResult.controller, request, routeResult.pathParams);
                System.out.println(result);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("not found");
        }
    }

    private final RouteRegistry router = new RouteRegistry();

    /**
     * search for all eligible entrances
     */
    public void register(IController controller) {
        Class<?> type = controller.getClass();
        for (Method method : type.getDeclaredMethods()) {
            RouteEndpoint routeEndpointAnno = method.getDeclaredAnnotation(RouteEndpoint.class);
            if (routeEndpointAnno != null) {
                CurlyBracketPattern.validateOrThrow(routeEndpointAnno.pathPattern());
                router.register(controller, method, routeEndpointAnno.httpMethod(), routeEndpointAnno.pathPattern());
            }
        }
    }
}
