package north.srs.server;

public interface FullRequestHandler {
    Response handle(Request request, RequestBody requestBody);
}
