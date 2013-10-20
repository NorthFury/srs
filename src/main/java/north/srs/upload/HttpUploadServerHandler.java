package north.srs.upload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import north.srs.route.Router;
import north.srs.server.Request;
import north.srs.server.RequestBody;

public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = Logger.getLogger(HttpUploadServerHandler.class.getName());
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private HttpRequest request;
    private RequestBody requestBody;
    private HttpPostRequestDecoder decoder;
    
    private final Router router;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        // system temp directory
        DiskFileUpload.baseDirectory = null;
        // should delete file on exit (in normal exit)
        DiskAttribute.deleteOnExitTemporaryFile = true;
        // system temp directory
        DiskAttribute.baseDirectory = null;
    }

    public HttpUploadServerHandler(Router router) {
        this.router = router;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        }

        if (msg instanceof HttpContent) {
            handleHttpContent(ctx, (HttpContent) msg);
        }

        if (msg instanceof LastHttpContent) {
            handleLastHttpContent(ctx, (LastHttpContent) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        this.request = request;

        routeRequest(ctx);
    }

    private void handleHttpContent(ChannelHandlerContext ctx, HttpContent chunk) throws Exception {
        if (decoder == null) {
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                writeResponse(ctx.channel(), e1.getMessage());
                ctx.channel().close();
            } catch (IncompatibleDataDecoderException e1) {
                // GET Method: should not try to create a HttpPostRequestDecoder
                writeResponse(ctx.channel(), e1.getMessage());
            }
        }
        if (requestBody == null) {
            requestBody = new RequestBody();
        }

        if (decoder != null) {
            try {
                decoder.offer(chunk);
            } catch (ErrorDataDecoderException e1) {
                writeResponse(ctx.channel(), e1.getMessage());
                ctx.channel().close();
            }
        }
    }

    private void handleLastHttpContent(ChannelHandlerContext ctx, LastHttpContent lastChunk) {
        if (decoder != null) {
            readDataFromDecoder();
        }
        requestBody.trailingHeaders().add(lastChunk.trailingHeaders());
        routeRequest(ctx);
        reset();
    }

    private void routeRequest(ChannelHandlerContext ctx) {
        String response;
        response = router.handleRequest(new Request(request));
        writeResponse(ctx.channel(), response);
    }

    private void reset() {
        if (decoder != null) {
            decoder.destroy();
            decoder = null;
        }
        request = null;
        requestBody = null;
    }

    private void readDataFromDecoder() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    switch (data.getHttpDataType()) {
                        case Attribute:
                            requestBody.attributes().add((Attribute) data);
                            break;
                        case FileUpload:
                            requestBody.files().add((FileUpload) data);
                            break;
                    }
                }
            }
        } catch (EndOfDataDecoderException e1) {
        }
    }

    private void writeResponse(Channel channel, String responseText) {
        ByteBuf buf = Unpooled.copiedBuffer(responseText, CharsetUtil.UTF_8);

        // Decide whether to close the connection or not.
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.headers().get(HttpHeaders.Names.CONNECTION))
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.headers().get(HttpHeaders.Names.CONNECTION));

        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
        }

        Set<Cookie> cookies;
        String value = request.headers().get(HttpHeaders.Names.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = CookieDecoder.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                response.headers().add(HttpHeaders.Names.SET_COOKIE, ServerCookieEncoder.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.log(Level.WARNING, cause.getMessage(), cause);
        ctx.channel().close();
    }
}
