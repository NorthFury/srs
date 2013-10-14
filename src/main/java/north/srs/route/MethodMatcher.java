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
    public boolean apply(HttpRequest request) {
        return method.equals(request.getMethod());
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodMatcher) {
            MethodMatcher that = (MethodMatcher) obj;
            return method.equals(that.method);
        }
        return false;
    }
}
