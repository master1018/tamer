public class RequestWrapper extends AbstractHttpMessage implements HttpUriRequest {
    private final HttpRequest original;
    private URI uri;
    private String method;
    private ProtocolVersion version;
    private int execCount;
    public RequestWrapper(final HttpRequest request) throws ProtocolException {
        super();
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        this.original = request;
        setParams(request.getParams());
        if (request instanceof HttpUriRequest) {
            this.uri = ((HttpUriRequest) request).getURI();
            this.method = ((HttpUriRequest) request).getMethod();
            this.version = null;
        } else {
            RequestLine requestLine = request.getRequestLine();
            try {
                this.uri = new URI(requestLine.getUri());
            } catch (URISyntaxException ex) {
                throw new ProtocolException("Invalid request URI: " 
                        + requestLine.getUri(), ex);
            }
            this.method = requestLine.getMethod();
            this.version = request.getProtocolVersion();
        }
        this.execCount = 0;
    }
    public void resetHeaders() {
        this.headergroup.clear();
        setHeaders(this.original.getAllHeaders());
    }
    public String getMethod() {
        return this.method;
    }
    public void setMethod(final String method) {
        if (method == null) {
            throw new IllegalArgumentException("Method name may not be null");
        }
        this.method = method;
    }
    public ProtocolVersion getProtocolVersion() {
        if (this.version != null) {
            return this.version;
        } else {
            return HttpProtocolParams.getVersion(getParams());
        }
    }
    public void setProtocolVersion(final ProtocolVersion version) {
        this.version = version;
    }
    public URI getURI() {
        return this.uri;
    }
    public void setURI(final URI uri) {
        this.uri = uri;
    }
    public RequestLine getRequestLine() {
        String method = getMethod();
        ProtocolVersion ver = getProtocolVersion();
        String uritext = null;
        if (uri != null) {
            uritext = uri.toASCIIString();
        }
        if (uritext == null || uritext.length() == 0) {
            uritext = "/";
        }
        return new BasicRequestLine(method, uritext, ver);
    }
    public void abort() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    public boolean isAborted() {
        return false;
    }
    public HttpRequest getOriginal() {
        return this.original;
    }
    public boolean isRepeatable() {
        return true;
    }
    public int getExecCount() {
        return this.execCount;
    }
    public void incrementExecCount() {
        this.execCount++;
    }
}
