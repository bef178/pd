package pd.server.demo.httpechoservice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.server.demo.httpechoservice.controller.Controller;
import pd.server.http.HttpRequestContext;
import pd.util.CurlyBracketPatternExtension;

public class RouteRegistry {

    public static class RouteEntry {

        public Controller controller;
        public Method method;
        public String requestVerb;
        public String requestPathPattern;
    }

    public static class RouteResult {

        public Controller controller;
        public Method method;
        public Map<String, String> pathParams;
    }

    private final HashMap<String, List<RouteEntry>> registry = new HashMap<String, List<RouteEntry>>();

    public void register(Controller controller, Method method, String requestVerb, String requestPathPattern) {
        List<RouteEntry> slot = registry.get(requestVerb);
        if (slot == null) {
            slot = new LinkedList<>();
            registry.put(requestVerb, slot);
        }
        RouteEntry entry = new RouteEntry();
        entry.controller = controller;
        entry.method = method;
        entry.requestVerb = requestVerb;
        entry.requestPathPattern = requestPathPattern;
        slot.add(entry);
    }

    public RouteResult route(HttpRequestContext request) {
        String requestPath = request.requestPath;
        List<RouteEntry> slot = registry.get(request.httpMethod);
        if (slot != null) {
            for (RouteEntry entry : slot) {
                Map<String, String> pathParams = CurlyBracketPatternExtension.match(entry.requestPathPattern, requestPath);
                if (pathParams != null) {
                    RouteResult result = new RouteResult();
                    result.controller = entry.controller;
                    result.method = entry.method;
                    result.pathParams = pathParams;
                    return result;
                }
            }
        }
        return null;
    }
}
