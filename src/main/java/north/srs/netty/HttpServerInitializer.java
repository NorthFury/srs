package north.srs.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import north.srs.route.Router;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final Router router;

    public HttpServerInitializer(Router router) {
        this.router = router;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();

//        if (HttpUploadServer.isSSL) {
//            SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
//            engine.setUseClientMode(false);
//            pipeline.addLast("ssl", new SslHandler(engine));
//        }
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());

        pipeline.addLast("deflater", new HttpContentCompressor());

        pipeline.addLast("handler", new HttpServerHandler(router));
    }
}
