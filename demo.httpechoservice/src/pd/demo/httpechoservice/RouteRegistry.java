package pd.demo.httpechoservice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.demo.httpechoservice.controller.IController;
import pd.net.http.HttpRequestContext;

public class RouteRegistry {

    public class RouteEntry {

        public IController controller;
        public Method method;
        public String requestVerb;
        public String requestPathPattern;
    }

    public class RouteResult {

        public IController controller;
        public Method method;
        public List<Map.Entry<String, String>> pathParams;
    }

    private final HashMap<String, List<RouteEntry>> registry = new HashMap<String, List<RouteEntry>>();

    public void register(IController controller, Method method, String requestVerb, String requestPathPattern) {
        var slot = registry.get(requestVerb);
        if (slot == null) {
            slot = new LinkedList<RouteEntry>();
            registry.put(requestVerb, slot);
        }
        var entry = new RouteEntry();
        entry.controller = controller;
        entry.method = method;
        entry.requestVerb = requestVerb;
        entry.requestPathPattern = entry.requestPathPattern;
        slot.add(entry);
    }

    public RouteResult route(HttpRequestContext request) {
        String requestPath = request.mRequestPath;
        List<RouteEntry> slot = registry.get(request.mHttpMethod);
        if (slot != null) {
            for (RouteEntry entry : slot) {
                var pathParams = CurlyBracePattern.matchRequestPath(entry.requestPathPattern, requestPath);
                if (pathParams != null) {
                    var result = new RouteResult();
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
