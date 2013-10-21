package north.srs.route;

import north.srs.route.matcher.Matcher;
import north.srs.server.FullRequestHandler;
import north.srs.server.Request;
import north.srs.server.RequestBody;
import north.srs.server.RequestHandler;
import north.srs.server.Response;

public class Route {

    private final Matcher matcher;
    private RequestHandler requestHandler;
    private FullRequestHandler fullRequestHandler;
    private final boolean requestBodyRequired;

    public Route(Matcher matcher, RequestHandler requestHandler) {
        this.matcher = matcher;
        this.requestHandler = requestHandler;
        requestBodyRequired = false;
    }

    public Route(Matcher matcher, FullRequestHandler fullRequestHandler) {
        this.matcher = matcher;
        this.fullRequestHandler = fullRequestHandler;
        requestBodyRequired = true;
    }

    public Response handle(Request request) {
        if (!matcher.apply(request)) {
            return Response.CONTINUE;
        }
        if (requestBodyRequired) {
            return Response.BODY_REQUIRED;
        }

        return requestHandler.handle(request);
    }

    public Response handle(Request request, RequestBody requestBody) {
        if (!matcher.apply(request)) {
            return Response.CONTINUE;
        }
        if (requestBodyRequired) {
            return fullRequestHandler.handle(request, requestBody);
        } else {
            return requestHandler.handle(request);
        }
    }
}
