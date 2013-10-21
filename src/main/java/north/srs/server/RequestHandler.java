package north.srs.server;

public interface RequestHandler {

    Response handle(Request request);
}
