package north.srs.server;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public class Request {

    private HttpVersion version;
    private HttpMethod method;
    private String uri;
    private final HttpHeaders headers = new DefaultHttpHeaders();

    public Request(HttpVersion version, HttpMethod method, String uri) {
        this.version = version;
        this.method = method;
        this.uri = uri;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}
