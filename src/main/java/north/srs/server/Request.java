package north.srs.server;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Request {

    private final HttpVersion version;
    private final HttpMethod method;
    private final URI uri;
    private final HttpHeaders headers;

    private final Map<String, List<String>> parameters;

    public Request(HttpRequest request) throws URISyntaxException {
        this(request.getProtocolVersion(), request.getMethod(), request.getUri(), request.headers());
    }

    public Request(HttpVersion version, HttpMethod method, String uri) throws URISyntaxException {
        this(version, method, uri, new DefaultHttpHeaders());
    }

    public Request(HttpVersion version, HttpMethod method, String uri, HttpHeaders headers) throws URISyntaxException {
        this(version, method, new URI(uri), headers);
    }

    public Request(HttpVersion version, HttpMethod method, URI uri) {
        this(version, method, uri, new DefaultHttpHeaders());
    }

    public Request(HttpVersion version, HttpMethod method, URI uri, HttpHeaders headers) {
        this.version = version;
        this.method = method;
        this.uri = uri;
        this.headers = headers;

        this.parameters = new QueryStringDecoder(uri).parameters();
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

    public HttpHeaders headers() {
        return headers;
    }

    public Map<String, List<String>> parameters() {
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
