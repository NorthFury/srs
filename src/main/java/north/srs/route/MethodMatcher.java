package north.srs.route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

/**
 *
 * @author North
 */
public class MethodMatcher extends Matcher {

    private final HttpMethod method;

    public MethodMatcher(HttpMethod method) {
        this.method = method;
    }

    @Override
    public boolean match(HttpRequest request) {
        return method.equals(request.getMethod());
    }
}
