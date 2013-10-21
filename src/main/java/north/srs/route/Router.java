package north.srs.route;

import java.util.LinkedHashMap;
import java.util.Map;
import north.srs.route.matcher.Matcher;
import north.srs.server.Request;
import north.srs.server.RequestHandler;
import north.srs.server.Response;

public class Router {

    private final Map<Matcher, RequestHandler> routes = new LinkedHashMap<>();
    private RequestHandler defaultHandler;

    public Router() {
        defaultHandler = (Request request) -> new Response("404");
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

    public Response handleRequest(Request request) {
        for (Matcher matcher : routes.keySet()) {
            if (matcher.apply(request)) {
                return routes.get(matcher).handle(request);
            }
        }

        return defaultHandler.handle(request);
    }
}
