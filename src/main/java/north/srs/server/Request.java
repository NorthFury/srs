package north.srs.server;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Request {

    private final HttpVersion version;
    private final HttpMethod method;
    private final String uri;
    private final HttpHeaders headers;

    private Map<String, List<String>> parameters;

    public Request(HttpRequest request) {
        this(request.getProtocolVersion(), request.getMethod(), request.getUri(), request.headers());
    }

    public Request(HttpVersion version, HttpMethod method, String uri) {
        this(version, method, uri, new DefaultHttpHeaders());
    }

    public Request(HttpVersion version, HttpMethod method, String uri, HttpHeaders headers) {
        this.version = version;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public Map<String, List<String>> parameters() {
        if (this.parameters == null) {
            this.parameters = new QueryStringDecoder(uri).parameters();
        }
        return parameters;
    }

    public Set<Cookie> cookies() {
        String value = headers.get(HttpHeaders.Names.COOKIE);
        if (value == null) {
            return Collections.emptySet();
        } else {
            return CookieDecoder.decode(value);
        }
    }
}
