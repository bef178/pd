package pd.server.demo.httpechoservice;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import pd.logger.Logger;
import pd.logger.LoggerManager;
import pd.server.SomeServer;
import pd.server.demo.httpechoservice.RouteRegistry.RouteResult;
import pd.server.demo.httpechoservice.annotation.RouteEndpoint;
import pd.server.demo.httpechoservice.controller.EchoController;
import pd.server.demo.httpechoservice.controller.Controller;
import pd.server.http.HttpRequestContext;

public class SomeApplication extends SomeServer<HttpRequestContext> {

    public static void main(String[] args) throws IOException {
        SomeApplication app = new SomeApplication(LoggerManager.singleton().getLogger());
        app.register(new EchoController());
        app.start(8881);
    }

    public SomeApplication(Logger logger) {
        super(logger);
    }

    @Override
    protected HttpRequestContext buildRequest(Socket socket) throws IOException {
        return new HttpRequestContext(socket);
    }

    @Override
    protected void executeRequest(HttpRequestContext request) {
        RouteResult routeResult = router.route(request);
        if (routeResult != null) {
            try {
                Object result = routeResult.method.invoke(routeResult.controller, request, routeResult.pathParams);
                logger.info("Controller result: {}", result);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            logger.warning("Controller not found");
        }
    }

    private final RouteRegistry router = new RouteRegistry();

    /**
     * search for all eligible entrances
     */
    public void register(Controller controller) {
        Class<?> type = controller.getClass();
        for (Method method : type.getDeclaredMethods()) {
            RouteEndpoint annoRouteEndpoint = method.getDeclaredAnnotation(RouteEndpoint.class);
            if (annoRouteEndpoint != null) {
                router.register(controller, method, annoRouteEndpoint.httpMethod(), annoRouteEndpoint.pathPattern());
            }
        }
    }
}
