package north.srs.server;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import java.util.ArrayList;
import java.util.List;

public class RequestBody {

    private final HttpHeaders trailingHeaders = new DefaultHttpHeaders();
    private final List<Attribute> attributes = new ArrayList<>();
    private final List<FileUpload> files = new ArrayList<>();

    public HttpHeaders trailingHeaders() {
        return trailingHeaders;
    }

    public List<Attribute> attributes() {
        return attributes;
    }

    public List<FileUpload> files() {
        return files;
    }
}
