public class DefaultClientConnection extends SocketHttpClientConnection
    implements OperatedClientConnection {
    private final Log log = LogFactory.getLog(getClass());
    private final Log headerLog = LogFactory.getLog("org.apache.http.headers");
    private final Log wireLog = LogFactory.getLog("org.apache.http.wire");
    private volatile Socket socket;
    private HttpHost targetHost;
    private boolean connSecure;
    private volatile boolean shutdown;
    public DefaultClientConnection() {
        super();
    }
    public final HttpHost getTargetHost() {
        return this.targetHost;
    }
    public final boolean isSecure() {
        return this.connSecure;
    }
    @Override
    public final Socket getSocket() {
        return this.socket;
    }
    public void opening(Socket sock, HttpHost target) throws IOException {
        assertNotOpen();        
        this.socket = sock;
        this.targetHost = target;
        if (this.shutdown) {
            sock.close(); 
            throw new IOException("Connection already shutdown");
        }
    }
    public void openCompleted(boolean secure, HttpParams params) throws IOException {
        assertNotOpen();
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        this.connSecure = secure;
        bind(this.socket, params);
    }
    @Override
    public void shutdown() throws IOException {
        log.debug("Connection shut down");
        shutdown = true;
        super.shutdown();        
        Socket sock = this.socket; 
        if (sock != null)
            sock.close();
    } 
    @Override
    public void close() throws IOException {
        log.debug("Connection closed");
        super.close();
    }
    @Override
    protected SessionInputBuffer createSessionInputBuffer(
            final Socket socket,
            int buffersize,
            final HttpParams params) throws IOException {
        SessionInputBuffer inbuffer = super.createSessionInputBuffer(
                socket, 
                buffersize,
                params);
        if (wireLog.isDebugEnabled()) {
            inbuffer = new LoggingSessionInputBuffer(inbuffer, new Wire(wireLog));
        }
        return inbuffer;
    }
    @Override
    protected SessionOutputBuffer createSessionOutputBuffer(
            final Socket socket,
            int buffersize,
            final HttpParams params) throws IOException {
        SessionOutputBuffer outbuffer = super.createSessionOutputBuffer(
                socket,
                buffersize,
                params);
        if (wireLog.isDebugEnabled()) {
            outbuffer = new LoggingSessionOutputBuffer(outbuffer, new Wire(wireLog));
        }
        return outbuffer;
    }
    @Override
    protected HttpMessageParser createResponseParser(
            final SessionInputBuffer buffer,
            final HttpResponseFactory responseFactory, 
            final HttpParams params) {
        return new DefaultResponseParser
            (buffer, null, responseFactory, params);
    }
    public void update(Socket sock, HttpHost target,
                       boolean secure, HttpParams params)
        throws IOException {
        assertOpen();
        if (target == null) {
            throw new IllegalArgumentException
                ("Target host must not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        if (sock != null) {
            this.socket = sock;
            bind(sock, params);
        }
        targetHost = target;
        connSecure = secure;
    } 
    @Override
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        HttpResponse response = super.receiveResponseHeader();
        if (headerLog.isDebugEnabled()) {
            headerLog.debug("<< " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerLog.debug("<< " + header.toString());
            }
        }
        return response;
    }
    @Override
    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        super.sendRequestHeader(request);
        if (headerLog.isDebugEnabled()) {
            headerLog.debug(">> " + request.getRequestLine().toString());
            Header[] headers = request.getAllHeaders();
            for (Header header : headers) {
                headerLog.debug(">> " + header.toString());
            }
        }
    }
} 
