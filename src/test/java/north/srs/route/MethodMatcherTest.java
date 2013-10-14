package north.srs.route;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

public class MethodMatcherTest {

    @Test
    public void testMatchTrue() {
        MethodMatcher matcher = new MethodMatcher(HttpMethod.GET);
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");

        Assert.assertEquals(true, matcher.apply(request));
    }

    @Test
    public void testMatchFalse() {
        MethodMatcher matcher = new MethodMatcher(HttpMethod.GET);
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "");

        Assert.assertEquals(false, matcher.apply(request));
    }

}
