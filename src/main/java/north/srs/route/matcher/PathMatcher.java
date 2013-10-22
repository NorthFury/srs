package north.srs.route.matcher;

import north.srs.server.Request;

public class PathMatcher extends Matcher {

    private final String path;

    public PathMatcher(String path) {
        this.path = path;
    }

    @Override
    public boolean apply(Request request) {
        return path.equals(request.getUri());
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathMatcher) {
            PathMatcher that = (PathMatcher) obj;
            return path.equals(that.path);
        }
        return false;
    }
}
