public class ThreadSafeClientConnManager implements ClientConnectionManager {
    private final Log log = LogFactory.getLog(getClass());
    protected SchemeRegistry schemeRegistry; 
    protected final AbstractConnPool connectionPool;
    protected ClientConnectionOperator connOperator;
    public ThreadSafeClientConnManager(HttpParams params,
                                       SchemeRegistry schreg) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.schemeRegistry = schreg;
        this.connOperator   = createConnectionOperator(schreg);
        this.connectionPool = createConnectionPool(params);
    } 
    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }
    protected AbstractConnPool createConnectionPool(final HttpParams params) {
        AbstractConnPool acp = new ConnPoolByRoute(connOperator, params);
        boolean conngc = true; 
        if (conngc) {
            acp.enableConnectionGC();
        }
        return acp;
    }
    protected ClientConnectionOperator
        createConnectionOperator(SchemeRegistry schreg) {
        return new DefaultClientConnectionOperator(schreg);
    }
    public SchemeRegistry getSchemeRegistry() {
        return this.schemeRegistry;
    }
    public ClientConnectionRequest requestConnection(
            final HttpRoute route, 
            final Object state) {
        final PoolEntryRequest poolRequest = connectionPool.requestPoolEntry(
                route, state);
        return new ClientConnectionRequest() {
            public void abortRequest() {
                poolRequest.abortRequest();
            }
            public ManagedClientConnection getConnection(
                    long timeout, TimeUnit tunit) throws InterruptedException,
                    ConnectionPoolTimeoutException {
                if (route == null) {
                    throw new IllegalArgumentException("Route may not be null.");
                }
                if (log.isDebugEnabled()) {
                    log.debug("ThreadSafeClientConnManager.getConnection: "
                        + route + ", timeout = " + timeout);
                }
                BasicPoolEntry entry = poolRequest.getPoolEntry(timeout, tunit);
                return new BasicPooledConnAdapter(ThreadSafeClientConnManager.this, entry);
            }
        };
    }
    public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit) {
        if (!(conn instanceof BasicPooledConnAdapter)) {
            throw new IllegalArgumentException
                ("Connection class mismatch, " +
                 "connection not obtained from this manager.");
        }
        BasicPooledConnAdapter hca = (BasicPooledConnAdapter) conn;
        if ((hca.getPoolEntry() != null) && (hca.getManager() != this)) {
            throw new IllegalArgumentException
                ("Connection not obtained from this manager.");
        }
        try {
            if (hca.isOpen() && !hca.isMarkedReusable()) {
                if (log.isDebugEnabled()) {
                    log.debug
                        ("Released connection open but not marked reusable.");
                }
                hca.shutdown();
            }
        } catch (IOException iox) {
            if (log.isDebugEnabled())
                log.debug("Exception shutting down released connection.",
                          iox);
        } finally {
            BasicPoolEntry entry = (BasicPoolEntry) hca.getPoolEntry();
            boolean reusable = hca.isMarkedReusable();
            hca.detach();
            if (entry != null) {
                connectionPool.freeEntry(entry, reusable, validDuration, timeUnit);
            }
        }
    }
    public void shutdown() {
        connectionPool.shutdown();
    }
    public int getConnectionsInPool(HttpRoute route) {
        return ((ConnPoolByRoute)connectionPool).getConnectionsInPool(
                route);
    }
    public int getConnectionsInPool() {
        synchronized (connectionPool) {
            return connectionPool.numConnections; 
        }
    }
    public void closeIdleConnections(long idleTimeout, TimeUnit tunit) {
        connectionPool.closeIdleConnections(idleTimeout, tunit);
        connectionPool.deleteClosedConnections();
    }
    public void closeExpiredConnections() {
        connectionPool.closeExpiredConnections();
        connectionPool.deleteClosedConnections();
    }
} 
