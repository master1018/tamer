public abstract class AbstractPoolEntry {
    protected final ClientConnectionOperator connOperator;
    protected final OperatedClientConnection connection;
    protected volatile HttpRoute route;
    protected volatile Object state;
    protected volatile RouteTracker tracker;
    protected AbstractPoolEntry(ClientConnectionOperator connOperator,
                                HttpRoute route) {
        super();
        if (connOperator == null) {
            throw new IllegalArgumentException("Connection operator may not be null");
        }
        this.connOperator = connOperator;
        this.connection = connOperator.createConnection();
        this.route = route;
        this.tracker = null;
    }
    public Object getState() {
        return state;
    }
    public void setState(final Object state) {
        this.state = state;
    }
    public void open(HttpRoute route,
                     HttpContext context, HttpParams params)
        throws IOException {
        if (route == null) {
            throw new IllegalArgumentException
                ("Route must not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        if ((this.tracker != null) && this.tracker.isConnected()) {
            throw new IllegalStateException("Connection already open.");
        }
        this.tracker = new RouteTracker(route);
        final HttpHost proxy  = route.getProxyHost();
        connOperator.openConnection
            (this.connection,
             (proxy != null) ? proxy : route.getTargetHost(),
             route.getLocalAddress(),
             context, params);
        RouteTracker localTracker = tracker; 
        if (localTracker == null) {
            throw new IOException("Request aborted");
        }
        if (proxy == null) {
            localTracker.connectTarget(this.connection.isSecure());
        } else {
            localTracker.connectProxy(proxy, this.connection.isSecure());
        }
    } 
    public void tunnelTarget(boolean secure, HttpParams params)
        throws IOException {
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        if ((this.tracker == null) || !this.tracker.isConnected()) {
            throw new IllegalStateException("Connection not open.");
        }
        if (this.tracker.isTunnelled()) {
            throw new IllegalStateException
                ("Connection is already tunnelled.");
        }
        this.connection.update(null, tracker.getTargetHost(),
                               secure, params);
        this.tracker.tunnelTarget(secure);
    } 
    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params)
        throws IOException {
        if (next == null) {
            throw new IllegalArgumentException
                ("Next proxy must not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        if ((this.tracker == null) || !this.tracker.isConnected()) {
            throw new IllegalStateException("Connection not open.");
        }
        this.connection.update(null, next, secure, params);
        this.tracker.tunnelProxy(next, secure);
    } 
    public void layerProtocol(HttpContext context, HttpParams params)
        throws IOException {
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        if ((this.tracker == null) || !this.tracker.isConnected()) {
            throw new IllegalStateException("Connection not open.");
        }
        if (!this.tracker.isTunnelled()) {
            throw new IllegalStateException
                ("Protocol layering without a tunnel not supported.");
        }
        if (this.tracker.isLayered()) {
            throw new IllegalStateException
                ("Multiple protocol layering not supported.");
        }
        final HttpHost target = tracker.getTargetHost();
        connOperator.updateSecureConnection(this.connection, target,
                                             context, params);
        this.tracker.layerProtocol(this.connection.isSecure());
    } 
    protected void shutdownEntry() { 
        tracker = null;
    }
} 
