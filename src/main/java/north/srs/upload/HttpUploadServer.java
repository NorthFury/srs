package north.srs.upload;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import north.srs.route.matcher.Matcher;
import north.srs.route.matcher.Matchers;
import north.srs.route.matcher.MethodMatcher;
import north.srs.route.matcher.PathMatcher;
import north.srs.route.Router;
import north.srs.server.Response;

public class HttpUploadServer {

    private final int port;
    public static boolean isSSL;

    public HttpUploadServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new HttpUploadServerInitializer(createRouter()));

            Channel ch = b.bind(port).sync().channel();
            System.out.println("HTTP Upload Server at port " + port + '.');
            System.out.println("Open your browser and navigate to http://localhost:" + port + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private Router createRouter() {
        Router router = new Router();
        Matcher matcher = Matchers.and(
                new MethodMatcher(HttpMethod.GET),
                new PathMatcher("/hello")
        );
        router.addRoute(matcher, (request) -> new Response("Hello world."));
        return router;
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        if (args.length > 1) {
            isSSL = true;
        }
        new HttpUploadServer(port).run();
    }
}
