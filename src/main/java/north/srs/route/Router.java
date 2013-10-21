package north.srs.route;

import java.util.LinkedList;
import java.util.List;
import north.srs.route.matcher.Matcher;
import north.srs.server.Request;
import north.srs.server.RequestBody;
import north.srs.server.RequestHandler;
import north.srs.server.Response;

public class Router {

    private final List<Route> routes = new LinkedList<>();
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
        routes.add(new Route(matcher, handler));
    }

    public Response handleRequest(Request request) {
        for (Route route : routes) {
            Response response = route.handle(request);
            if (response.equals(Response.BODY_REQUIRED) || !response.equals(Response.CONTINUE))  {
                return response;
            }
        }

        return defaultHandler.handle(request);
    }

    public Response handleRequest(Request request, RequestBody requestBody) {
        for (Route route : routes) {
            Response response = route.handle(request);
            if (!response.equals(Response.CONTINUE))  {
                return response;
            }
        }

        return defaultHandler.handle(request);
    }
}
