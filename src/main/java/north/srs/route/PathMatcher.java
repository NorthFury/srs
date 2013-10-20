package north.srs.route;

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

}
