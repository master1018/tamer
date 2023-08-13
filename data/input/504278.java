public class SingleClientConnManager implements ClientConnectionManager {
    private final Log log = LogFactory.getLog(getClass());
    public final static String MISUSE_MESSAGE =
    "Invalid use of SingleClientConnManager: connection still allocated.\n" +
    "Make sure to release the connection before allocating another one.";
    protected SchemeRegistry schemeRegistry; 
    protected ClientConnectionOperator connOperator;
    protected PoolEntry uniquePoolEntry;
    protected ConnAdapter managedConn;
    protected long lastReleaseTime;
    protected long connectionExpiresTime;
    protected boolean alwaysShutDown;
    protected volatile boolean isShutDown;
    public SingleClientConnManager(HttpParams params,
                                   SchemeRegistry schreg) {
        if (schreg == null) {
            throw new IllegalArgumentException
                ("Scheme registry must not be null.");
        }
        this.schemeRegistry  = schreg;
        this.connOperator    = createConnectionOperator(schreg);
        this.uniquePoolEntry = new PoolEntry();
        this.managedConn     = null;
        this.lastReleaseTime = -1L;
        this.alwaysShutDown  = false; 
        this.isShutDown      = false;
    } 
    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }
    public SchemeRegistry getSchemeRegistry() {
        return this.schemeRegistry;
    }
    protected ClientConnectionOperator
        createConnectionOperator(SchemeRegistry schreg) {
        return new DefaultClientConnectionOperator(schreg);
    }
    protected final void assertStillUp()
        throws IllegalStateException {
        if (this.isShutDown)
            throw new IllegalStateException("Manager is shut down.");
    }
    public final ClientConnectionRequest requestConnection(
            final HttpRoute route,
            final Object state) {
        return new ClientConnectionRequest() {
            public void abortRequest() {
            }
            public ManagedClientConnection getConnection(
                    long timeout, TimeUnit tunit) {
                return SingleClientConnManager.this.getConnection(
                        route, state);
            }
        };
    }
    public ManagedClientConnection getConnection(HttpRoute route, Object state) {
        if (route == null) {
            throw new IllegalArgumentException("Route may not be null.");
        }
        assertStillUp();
        if (log.isDebugEnabled()) {
            log.debug("Get connection for route " + route);
        }
        if (managedConn != null)
            revokeConnection();
        boolean recreate = false;
        boolean shutdown = false;
        closeExpiredConnections();
        if (uniquePoolEntry.connection.isOpen()) {
            RouteTracker tracker = uniquePoolEntry.tracker;
            shutdown = (tracker == null || 
                        !tracker.toRoute().equals(route));
        } else {
            recreate = true;
        }
        if (shutdown) {
            recreate = true;
            try {
                uniquePoolEntry.shutdown();
            } catch (IOException iox) {
                log.debug("Problem shutting down connection.", iox);
            }
        }
        if (recreate)
            uniquePoolEntry = new PoolEntry();
        managedConn = new ConnAdapter(uniquePoolEntry, route);
        return managedConn;
    }
    public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit) {
        assertStillUp();
        if (!(conn instanceof ConnAdapter)) {
            throw new IllegalArgumentException
                ("Connection class mismatch, " +
                 "connection not obtained from this manager.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Releasing connection " + conn);
        }
        ConnAdapter sca = (ConnAdapter) conn;
        if (sca.poolEntry == null)
            return; 
        ClientConnectionManager manager = sca.getManager();
        if (manager != null && manager != this) {
            throw new IllegalArgumentException
                ("Connection not obtained from this manager.");
        }
        try {
            if (sca.isOpen() && (this.alwaysShutDown ||
                                 !sca.isMarkedReusable())
                ) {
                if (log.isDebugEnabled()) {
                    log.debug
                        ("Released connection open but not reusable.");
                }
                sca.shutdown();
            }
        } catch (IOException iox) {
            if (log.isDebugEnabled())
                log.debug("Exception shutting down released connection.",
                          iox);
        } finally {
            sca.detach();
            managedConn = null;
            lastReleaseTime = System.currentTimeMillis();
            if(validDuration > 0)
                connectionExpiresTime = timeUnit.toMillis(validDuration) + lastReleaseTime;
            else
                connectionExpiresTime = Long.MAX_VALUE;
        }
    } 
    public void closeExpiredConnections() {
        if(System.currentTimeMillis() >= connectionExpiresTime) {
            closeIdleConnections(0, TimeUnit.MILLISECONDS);
        }
    }
    public void closeIdleConnections(long idletime, TimeUnit tunit) {
        assertStillUp();
        if (tunit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        }
        if ((managedConn == null) && uniquePoolEntry.connection.isOpen()) {
            final long cutoff =
                System.currentTimeMillis() - tunit.toMillis(idletime);
            if (lastReleaseTime <= cutoff) {
                try {
                    uniquePoolEntry.close();
                } catch (IOException iox) {
                    log.debug("Problem closing idle connection.", iox);
                }
            }
        }
    }
    public void shutdown() {
        this.isShutDown = true;
        if (managedConn != null)
            managedConn.detach();
        try {
            if (uniquePoolEntry != null) 
                uniquePoolEntry.shutdown();
        } catch (IOException iox) {
            log.debug("Problem while shutting down manager.", iox);
        } finally {
            uniquePoolEntry = null;
        }
    }
    protected void revokeConnection() {
        if (managedConn == null)
            return;
        log.warn(MISUSE_MESSAGE);
        managedConn.detach();
        try {
            uniquePoolEntry.shutdown();
        } catch (IOException iox) {
            log.debug("Problem while shutting down connection.", iox);
        }
    }
    protected class PoolEntry extends AbstractPoolEntry {
        protected PoolEntry() {
            super(SingleClientConnManager.this.connOperator, null);
        }
        protected void close()
            throws IOException {
            shutdownEntry();
            if (connection.isOpen())
                connection.close();
        }
        protected void shutdown()
            throws IOException {
            shutdownEntry();
            if (connection.isOpen())
                connection.shutdown();
        }
    } 
    protected class ConnAdapter extends AbstractPooledConnAdapter {
        protected ConnAdapter(PoolEntry entry, HttpRoute route) {
            super(SingleClientConnManager.this, entry);
            markReusable();
            entry.route = route;
        }
    }
} 
