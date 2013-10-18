package north.srs.route;

import north.srs.server.Request;

public abstract class Matcher {

    public abstract boolean apply(Request request);
}
