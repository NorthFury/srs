package north.srs.route;

import java.util.LinkedHashMap;
import java.util.Map;
import north.srs.server.Request;
import north.srs.server.RequestHandler;

public class Router {

    private final Map<Matcher, RequestHandler> routes = new LinkedHashMap<>();
    private RequestHandler defaultHandler;

    public Router() {
        defaultHandler = (Request request) -> {
            return "404";
        };
    }

    public void setDefaultHandler(RequestHandler defaultHandler) {
        if (defaultHandler == null) {
            throw new NullPointerException("defaultHandler");
        }
        this.defaultHandler = defaultHandler;
    }

    public void addRoute(Matcher matcher, RequestHandler handler) {
        routes.put(matcher, handler);
    }

    public String handleRequest(Request request) {
        for (Matcher matcher : routes.keySet()) {
            if (matcher.apply(request)) {
                return routes.get(matcher).handle(request);
            }
        }

        return defaultHandler.handle(request);
    }
}
