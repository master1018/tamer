public abstract class AbstractClientConnAdapter
    implements ManagedClientConnection {
    private final Thread executionThread; 
    private volatile ClientConnectionManager connManager;
    private volatile OperatedClientConnection wrappedConnection;
    private volatile boolean markedReusable;
    private volatile boolean aborted;
    private volatile long duration;
    protected AbstractClientConnAdapter(ClientConnectionManager mgr,
                                        OperatedClientConnection conn) {
        super();
        executionThread = Thread.currentThread();
        connManager = mgr;
        wrappedConnection = conn;
        markedReusable = false;
        aborted = false;
        duration = Long.MAX_VALUE;
    } 
    protected void detach() {
        wrappedConnection = null;
        connManager = null; 
        duration = Long.MAX_VALUE;
    }
    protected OperatedClientConnection getWrappedConnection() {
        return wrappedConnection;
    }
    protected ClientConnectionManager getManager() {
        return connManager;
    }
    protected final void assertNotAborted() throws InterruptedIOException {
        if (aborted) {
            throw new InterruptedIOException("Connection has been shut down.");
        }
    }
    protected final void assertValid(
            final OperatedClientConnection wrappedConn) {
        if (wrappedConn == null) {
            throw new IllegalStateException("No wrapped connection.");
        }
    }
    public boolean isOpen() {
        OperatedClientConnection conn = getWrappedConnection();
        if (conn == null)
            return false;
        return conn.isOpen();
    }
    public boolean isStale() {
        if (aborted)
            return true;
        OperatedClientConnection conn = getWrappedConnection();
        if (conn == null)
            return true;
        return conn.isStale();
    }
    public void setSocketTimeout(int timeout) {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        conn.setSocketTimeout(timeout);
    }
    public int getSocketTimeout() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getSocketTimeout();
    }
    public HttpConnectionMetrics getMetrics() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getMetrics();
    }
    public void flush()
        throws IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        conn.flush();
    }
    public boolean isResponseAvailable(int timeout)
        throws IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.isResponseAvailable(timeout);
    }
    public void receiveResponseEntity(HttpResponse response)
        throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.receiveResponseEntity(response);
    }
    public HttpResponse receiveResponseHeader()
        throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        return conn.receiveResponseHeader();
    }
    public void sendRequestEntity(HttpEntityEnclosingRequest request)
        throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.sendRequestEntity(request);
    }
    public void sendRequestHeader(HttpRequest request)
        throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.sendRequestHeader(request);
    }
    public InetAddress getLocalAddress() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getLocalAddress();
    }
    public int getLocalPort() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getLocalPort();
    }
    public InetAddress getRemoteAddress() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getRemoteAddress();
    }
    public int getRemotePort() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getRemotePort();
    }
    public boolean isSecure() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.isSecure();
    }
    public SSLSession getSSLSession() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        if (!isOpen())
            return null;
        SSLSession result = null;
        Socket    sock    = conn.getSocket();
        if (sock instanceof SSLSocket) {
            result = ((SSLSocket)sock).getSession();
        }
        return result;
    }
    public void markReusable() {
        markedReusable = true;
    }
    public void unmarkReusable() {
        markedReusable = false;
    }
    public boolean isMarkedReusable() {
        return markedReusable;
    }
    public void setIdleDuration(long duration, TimeUnit unit) {
        if(duration > 0) {
            this.duration = unit.toMillis(duration);
        } else {
            this.duration = -1;
        }
    }
    public void releaseConnection() {
        if (connManager != null) {
            connManager.releaseConnection(this, duration, TimeUnit.MILLISECONDS);
        }
    }
    public void abortConnection() {
        if (aborted) {
            return;
        }
        aborted = true;
        unmarkReusable();
        try {
            shutdown();
        } catch (IOException ignore) {
        }
        if (executionThread.equals(Thread.currentThread())) {
            releaseConnection();
        }
    }
} 
