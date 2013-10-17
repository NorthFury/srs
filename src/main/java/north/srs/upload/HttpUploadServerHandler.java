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
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = Logger.getLogger(HttpUploadServerHandler.class.getName());
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private final StringBuilder responseContent = new StringBuilder();

    private HttpRequest request;
    private HttpPostRequestDecoder decoder;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        // system temp directory
        DiskFileUpload.baseDirectory = null;
        // should delete file on exit (in normal exit)
        DiskAttribute.deleteOnExitTemporaryFile = true;
        // system temp directory
        DiskAttribute.baseDirectory = null;
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
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        this.request = request;

        boolean isChunked = HttpHeaders.isTransferEncodingChunked(request);
        boolean isMultipart = decoder.isMultipart();
    }

    private void handleHttpContent(ChannelHandlerContext ctx, HttpContent chunk) throws Exception {
        if (decoder == null) {
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                writeResponse(ctx.channel());
                ctx.channel().close();
            } catch (IncompatibleDataDecoderException e1) {
                // GET Method: should not try to create a HttpPostRequestDecoder
                writeResponse(ctx.channel());
            }
        }

        if (decoder != null) {
            try {
                decoder.offer(chunk);
            } catch (ErrorDataDecoderException e1) {
                writeResponse(ctx.channel());
                ctx.channel().close();
                return;
            }
            // example of reading chunk by chunk (minimize memory usage due to Factory)
            readHttpDataChunkByChunk();
            // example of reading only if at the end
            if (chunk instanceof LastHttpContent) {
                writeResponse(ctx.channel());
                reset();
            }
        }
    }

    private void reset() {
        request = null;

        decoder.destroy();
        decoder = null;
    }

    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        // new value
                        writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (EndOfDataDecoderException e1) {
            // end
            responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }

    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                responseContent.append("\r\nBODY Attribute: ").append(attribute.getHttpDataType().name()).append(": ").append(attribute.getName()).append(" Error while reading value: ").append(e1.getMessage()).append("\r\n");
                return;
            }
            if (value.length() > 100) {
                responseContent.append("\r\nBODY Attribute: ").append(attribute.getHttpDataType().name()).append(": ").append(attribute.getName()).append(" data too long\r\n");
            } else {
                responseContent.append("\r\nBODY Attribute: ").append(attribute.getHttpDataType().name()).append(": ").append(attribute.toString()).append("\r\n");
            }
        } else {
            responseContent.append("\r\nBODY FileUpload: ").append(data.getHttpDataType().name()).append(": ").append(data.toString()).append("\r\n");
            if (data.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    if (fileUpload.length() < 10000) {
                        responseContent.append("\tContent of file\r\n");
                        try {
                            responseContent.append(fileUpload.getString(fileUpload.getCharset()));
                        } catch (IOException e1) {
                        }
                        responseContent.append("\r\n");
                    } else {
                        responseContent.append("\tFile too long to be printed out:").append(fileUpload.length()).append("\r\n");
                    }
//                     fileUpload.isInMemory();// tells if the file is in Memory or on File
//                     fileUpload.renameTo(dest); // enable to move into another File dest
//                     decoder.removeHttpDataFromClean(fileUpload); //remove the File of to delete file
                } else {
                    responseContent.append("\tFile to be continued but should not!\r\n");
                }
            }
        }
    }

    private void writeResponse(Channel channel) {
        // Convert the response content to a ChannelBuffer.
        ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);

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
