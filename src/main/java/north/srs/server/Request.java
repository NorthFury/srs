package north.srs.server;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URI;
import java.net.URISyntaxException;

public class Request {

    private final HttpVersion version;
    private final HttpMethod method;
    private final URI uri;
    private final HttpHeaders headers = new DefaultHttpHeaders();

    public Request(HttpVersion version, HttpMethod method, URI uri) {
        this.version = version;
        this.method = method;
        this.uri = uri;
    }

    public Request(HttpVersion version, HttpMethod method, String uri) throws URISyntaxException {
        this.version = version;
        this.method = method;
        this.uri = new URI(uri);
    }

    public HttpVersion getVersion() {
        return version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}
