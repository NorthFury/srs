package north.srs.server;

public class Response {

    public static final Response CONTINUE = new Response("");
    public static final Response BODY_REQUIRED = new Response("");

    private final String body;

    public Response(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
