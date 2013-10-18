package north.srs.route;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URISyntaxException;
import north.srs.server.Request;
import org.junit.Assert;
import org.junit.Test;

public class MethodMatcherTest {

    @Test
    public void testMatchTrue() throws URISyntaxException {
        MethodMatcher matcher = new MethodMatcher(HttpMethod.GET);
        DefaultHttpRequest defaultRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        Request request = new Request(defaultRequest);

        Assert.assertEquals(true, matcher.apply(request));
    }

    @Test
    public void testMatchFalse() throws URISyntaxException {
        MethodMatcher matcher = new MethodMatcher(HttpMethod.GET);
        DefaultHttpRequest defaultRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");
        Request request = new Request(defaultRequest);

        Assert.assertEquals(false, matcher.apply(request));
    }

}
