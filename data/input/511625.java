public abstract class AbstractConnPool implements RefQueueHandler {
    private final Log log = LogFactory.getLog(getClass());
    protected final Lock poolLock;
    protected Set<BasicPoolEntryRef> issuedConnections;
    protected IdleConnectionHandler idleConnHandler;
    protected int numConnections;
    protected ReferenceQueue<Object> refQueue;
    private RefQueueWorker refWorker;
    protected volatile boolean isShutDown;
    protected AbstractConnPool() {
        issuedConnections = new HashSet<BasicPoolEntryRef>();
        idleConnHandler = new IdleConnectionHandler();
        boolean fair = false; 
        poolLock = new ReentrantLock(fair);
    }
    public void enableConnectionGC()
        throws IllegalStateException {
        if (refQueue != null) {
            throw new IllegalStateException("Connection GC already enabled.");
        }
        poolLock.lock();
        try {
            if (numConnections > 0) { 
                throw new IllegalStateException("Pool already in use.");
            }
        } finally {
            poolLock.unlock();
        }
        refQueue  = new ReferenceQueue<Object>();
        refWorker = new RefQueueWorker(refQueue, this);
        Thread t = new Thread(refWorker); 
        t.setDaemon(true);
        t.setName("RefQueueWorker@" + this);
        t.start();
    }
    public final
        BasicPoolEntry getEntry(
                HttpRoute route, 
                Object state,
                long timeout, 
                TimeUnit tunit)
                    throws ConnectionPoolTimeoutException, InterruptedException {
        return requestPoolEntry(route, state).getPoolEntry(timeout, tunit);
    }
    public abstract PoolEntryRequest requestPoolEntry(HttpRoute route, Object state);
    public abstract void freeEntry(BasicPoolEntry entry, boolean reusable, long validDuration, TimeUnit timeUnit)
        ;
    public void handleReference(Reference ref) {
        poolLock.lock();
        try {
            if (ref instanceof BasicPoolEntryRef) {
                final boolean lost = issuedConnections.remove(ref);
                if (lost) {
                    final HttpRoute route =
                        ((BasicPoolEntryRef)ref).getRoute();
                    if (log.isDebugEnabled()) {
                        log.debug("Connection garbage collected. " + route);
                    }
                    handleLostEntry(route);
                }
            }
        } finally {
            poolLock.unlock();
        }
    }
    protected abstract void handleLostEntry(HttpRoute route)
        ;
    public void closeIdleConnections(long idletime, TimeUnit tunit) {
        if (tunit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        }
        poolLock.lock();
        try {
            idleConnHandler.closeIdleConnections(tunit.toMillis(idletime));
        } finally {
            poolLock.unlock();
        }
    }
    public void closeExpiredConnections() {
        poolLock.lock();
        try {
            idleConnHandler.closeExpiredConnections();
        } finally {
            poolLock.unlock();
        }
    }
    public abstract void deleteClosedConnections()
        ;
    public void shutdown() {
        poolLock.lock();
        try {
            if (isShutDown)
                return;
            if (refWorker != null)
                refWorker.shutdown();
            Iterator<BasicPoolEntryRef> iter = issuedConnections.iterator();
            while (iter.hasNext()) {
                BasicPoolEntryRef per = iter.next();
                iter.remove();
                BasicPoolEntry entry = per.get();
                if (entry != null) {
                    closeConnection(entry.getConnection());
                }
            }
            idleConnHandler.removeAll();
            isShutDown = true;
        } finally {
            poolLock.unlock();
        }
    }
    protected void closeConnection(final OperatedClientConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (IOException ex) {
                log.debug("I/O error closing connection", ex);
            }
        }
    }
} 
