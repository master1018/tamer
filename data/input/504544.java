public class BasicHttpRequest extends AbstractHttpMessage implements HttpRequest {
    private final RequestLine requestline;
    private final String method;
    private final String uri;
    public BasicHttpRequest(final String method, final String uri) {
        super();
        if (method == null) {
            throw new IllegalArgumentException("Method name may not be null");
        }
        if (uri == null) {
            throw new IllegalArgumentException("Request URI may not be null");
        }
        this.method = method;
        this.uri = uri;
        this.requestline = null;
    }
    public BasicHttpRequest(final String method, final String uri, final ProtocolVersion ver) {
        this(new BasicRequestLine(method, uri, ver));
    }
    public BasicHttpRequest(final RequestLine requestline) {
        super();
        if (requestline == null) {
            throw new IllegalArgumentException("Request line may not be null");
        }
        this.requestline = requestline;
        this.method = requestline.getMethod();
        this.uri = requestline.getUri();
    }
    public ProtocolVersion getProtocolVersion() {
        if (this.requestline != null) {
            return this.requestline.getProtocolVersion();
        } else {
            return HttpProtocolParams.getVersion(getParams());
        }
    }
    public RequestLine getRequestLine() {
        if (this.requestline != null) {
            return this.requestline;
        } else {
            ProtocolVersion ver = HttpProtocolParams.getVersion(getParams());
            return new BasicRequestLine(this.method, this.uri, ver);
        }
    }
}
