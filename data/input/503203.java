public abstract class AbstractPooledConnAdapter extends AbstractClientConnAdapter {
    protected volatile AbstractPoolEntry poolEntry;
    protected AbstractPooledConnAdapter(ClientConnectionManager manager,
                                        AbstractPoolEntry entry) {
        super(manager, entry.connection);
        this.poolEntry = entry;
    }
    protected final void assertAttached() {
        if (poolEntry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
    }
    @Override
    protected void detach() {
        super.detach();
        poolEntry = null;
    }
    public HttpRoute getRoute() {
        assertAttached();
        return (poolEntry.tracker == null) ?
            null : poolEntry.tracker.toRoute();
    }
    public void open(HttpRoute route,
                     HttpContext context, HttpParams params)
        throws IOException {
        assertAttached();
        poolEntry.open(route, context, params);
    }
    public void tunnelTarget(boolean secure, HttpParams params)
        throws IOException {
        assertAttached();
        poolEntry.tunnelTarget(secure, params);
    }
    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params)
        throws IOException {
        assertAttached();
        poolEntry.tunnelProxy(next, secure, params);
    }
    public void layerProtocol(HttpContext context, HttpParams params)
        throws IOException {
        assertAttached();
        poolEntry.layerProtocol(context, params);
    }
    public void close() throws IOException {
        if (poolEntry != null)
            poolEntry.shutdownEntry();
        OperatedClientConnection conn = getWrappedConnection();
        if (conn != null) {
            conn.close();
        }
    }
    public void shutdown() throws IOException {
        if (poolEntry != null)
            poolEntry.shutdownEntry();
        OperatedClientConnection conn = getWrappedConnection();
        if (conn != null) {
            conn.shutdown();
        }
    }
    public Object getState() {
        assertAttached();
        return poolEntry.getState();
    }
    public void setState(final Object state) {
        assertAttached();
        poolEntry.setState(state);
    }
} 
