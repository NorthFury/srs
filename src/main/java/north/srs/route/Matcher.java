package north.srs.route;

import io.netty.handler.codec.http.HttpRequest;

public abstract class Matcher {

    public abstract boolean apply(HttpRequest request);
}
