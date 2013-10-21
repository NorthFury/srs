package north.srs.route.matcher;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URISyntaxException;
import north.srs.server.Request;
import org.junit.Assert;
import org.junit.Test;

public class PathMatcherTest {

    @Test
    public void testMatchExactPathTrue() throws URISyntaxException {
        PathMatcher matcher = new PathMatcher("/path");
        Request request = new Request(HttpVersion.HTTP_1_1, HttpMethod.GET, "/path");

        Assert.assertEquals(true, matcher.apply(request));
    }

    @Test
    public void testMatchExactPathFalse() throws URISyntaxException {
        PathMatcher matcher = new PathMatcher("/path");
        Request request = new Request(HttpVersion.HTTP_1_1, HttpMethod.GET, "/other");

        Assert.assertEquals(false, matcher.apply(request));
    }

}
