public class ConnPoolByRoute extends AbstractConnPool {
    private final Log log = LogFactory.getLog(getClass());
    protected final ClientConnectionOperator operator;
    protected Queue<BasicPoolEntry> freeConnections;
    protected Queue<WaitingThread> waitingThreads;
    protected final Map<HttpRoute, RouteSpecificPool> routeToPool;
    protected final int maxTotalConnections;
    private final ConnPerRoute connPerRoute;
    public ConnPoolByRoute(final ClientConnectionOperator operator, final HttpParams params) {
        super();
        if (operator == null) {
            throw new IllegalArgumentException("Connection operator may not be null");
        }
        this.operator = operator;
        freeConnections = createFreeConnQueue();
        waitingThreads  = createWaitingThreadQueue();
        routeToPool     = createRouteToPoolMap();
        maxTotalConnections = ConnManagerParams
            .getMaxTotalConnections(params);
        connPerRoute = ConnManagerParams
            .getMaxConnectionsPerRoute(params);
    }
    protected Queue<BasicPoolEntry> createFreeConnQueue() {
        return new LinkedList<BasicPoolEntry>();
    }
    protected Queue<WaitingThread> createWaitingThreadQueue() {
        return new LinkedList<WaitingThread>();
    }
    protected Map<HttpRoute, RouteSpecificPool> createRouteToPoolMap() {
        return new HashMap<HttpRoute, RouteSpecificPool>();
    }
    protected RouteSpecificPool newRouteSpecificPool(HttpRoute route) {
        return new RouteSpecificPool(route, connPerRoute.getMaxForRoute(route));
    }
    protected WaitingThread newWaitingThread(Condition cond,
                                             RouteSpecificPool rospl) {
        return new WaitingThread(cond, rospl);
    }
    protected RouteSpecificPool getRoutePool(HttpRoute route,
                                             boolean create) {
        RouteSpecificPool rospl = null;
        poolLock.lock();
        try {
            rospl = routeToPool.get(route);
            if ((rospl == null) && create) {
                rospl = newRouteSpecificPool(route);
                routeToPool.put(route, rospl);
            }
        } finally {
            poolLock.unlock();
        }
        return rospl;
    }
    public int getConnectionsInPool(HttpRoute route) {
        poolLock.lock();
        try {
            RouteSpecificPool rospl = getRoutePool(route, false);
            return (rospl != null) ? rospl.getEntryCount() : 0;
        } finally {
            poolLock.unlock();
        }
    }
    @Override
    public PoolEntryRequest requestPoolEntry(
            final HttpRoute route,
            final Object state) {
        final WaitingThreadAborter aborter = new WaitingThreadAborter();
        return new PoolEntryRequest() {
            public void abortRequest() {
                poolLock.lock();
                try {
                    aborter.abort();
                } finally {
                    poolLock.unlock();
                }
            }
            public BasicPoolEntry getPoolEntry(
                    long timeout,
                    TimeUnit tunit)
                        throws InterruptedException, ConnectionPoolTimeoutException {
                return getEntryBlocking(route, state, timeout, tunit, aborter);
            }
        };
    }
    protected BasicPoolEntry getEntryBlocking(
                                   HttpRoute route, Object state,
                                   long timeout, TimeUnit tunit,
                                   WaitingThreadAborter aborter)
        throws ConnectionPoolTimeoutException, InterruptedException {
        Date deadline = null;
        if (timeout > 0) {
            deadline = new Date
                (System.currentTimeMillis() + tunit.toMillis(timeout));
        }
        BasicPoolEntry entry = null;
        poolLock.lock();
        try {
            RouteSpecificPool rospl = getRoutePool(route, true);
            WaitingThread waitingThread = null;
            while (entry == null) {
                if (isShutDown) {
                    throw new IllegalStateException
                        ("Connection pool shut down.");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Total connections kept alive: " + freeConnections.size()); 
                    log.debug("Total issued connections: " + issuedConnections.size()); 
                    log.debug("Total allocated connection: " + numConnections + " out of " + maxTotalConnections);
                }
                entry = getFreeEntry(rospl, state);
                if (entry != null) {
                    break;
                }
                boolean hasCapacity = rospl.getCapacity() > 0; 
                if (log.isDebugEnabled()) {
                    log.debug("Available capacity: " + rospl.getCapacity() 
                            + " out of " + rospl.getMaxEntries()
                            + " [" + route + "][" + state + "]");
                }
                if (hasCapacity && numConnections < maxTotalConnections) {
                    entry = createEntry(rospl, operator);
                } else if (hasCapacity && !freeConnections.isEmpty()) {
                    deleteLeastUsedEntry();
                    entry = createEntry(rospl, operator);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Need to wait for connection" +
                                " [" + route + "][" + state + "]");
                    }
                    if (waitingThread == null) {
                        waitingThread =
                            newWaitingThread(poolLock.newCondition(), rospl);
                        aborter.setWaitingThread(waitingThread);
                    }
                    boolean success = false;
                    try {
                        rospl.queueThread(waitingThread);
                        waitingThreads.add(waitingThread);
                        success = waitingThread.await(deadline);
                    } finally {
                        rospl.removeThread(waitingThread);
                        waitingThreads.remove(waitingThread);
                    }
                    if (!success && (deadline != null) &&
                        (deadline.getTime() <= System.currentTimeMillis())) {
                        throw new ConnectionPoolTimeoutException
                            ("Timeout waiting for connection");
                    }
                }
            } 
        } finally {
            poolLock.unlock();
        }
        return entry;
    } 
    @Override
    public void freeEntry(BasicPoolEntry entry, boolean reusable, long validDuration, TimeUnit timeUnit) {
        HttpRoute route = entry.getPlannedRoute();
        if (log.isDebugEnabled()) {
            log.debug("Freeing connection" +                                 
                    " [" + route + "][" + entry.getState() + "]");
        }
        poolLock.lock();
        try {
            if (isShutDown) {
                closeConnection(entry.getConnection());
                return;
            }
            issuedConnections.remove(entry.getWeakRef());
            RouteSpecificPool rospl = getRoutePool(route, true);
            if (reusable) {
                rospl.freeEntry(entry);
                freeConnections.add(entry);
                idleConnHandler.add(entry.getConnection(), validDuration, timeUnit);
            } else {
                rospl.dropEntry();
                numConnections--;
            }
            notifyWaitingThread(rospl);
        } finally {
            poolLock.unlock();
        }
    } 
    protected BasicPoolEntry getFreeEntry(RouteSpecificPool rospl, Object state) {
        BasicPoolEntry entry = null;
        poolLock.lock();
        try {
            boolean done = false;
            while(!done) {
                entry = rospl.allocEntry(state);
                if (entry != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Getting free connection" 
                                + " [" + rospl.getRoute() + "][" + state + "]");
                    }
                    freeConnections.remove(entry);
                    boolean valid = idleConnHandler.remove(entry.getConnection());
                    if(!valid) {
                        if(log.isDebugEnabled())
                            log.debug("Closing expired free connection"
                                    + " [" + rospl.getRoute() + "][" + state + "]");
                        closeConnection(entry.getConnection());
                        rospl.dropEntry();
                        numConnections--;
                    } else {
                        issuedConnections.add(entry.getWeakRef());
                        done = true;
                    }
                } else {
                    done = true;
                    if (log.isDebugEnabled()) {
                        log.debug("No free connections" 
                                + " [" + rospl.getRoute() + "][" + state + "]");
                    }
                }
            }
        } finally {
            poolLock.unlock();
        }
        return entry;
    }
    protected BasicPoolEntry createEntry(RouteSpecificPool rospl,
                                         ClientConnectionOperator op) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new connection [" + rospl.getRoute() + "]");
        }
        BasicPoolEntry entry =
            new BasicPoolEntry(op, rospl.getRoute(), refQueue);
        poolLock.lock();
        try {
            rospl.createdEntry(entry);
            numConnections++;
            issuedConnections.add(entry.getWeakRef());
        } finally {
            poolLock.unlock();
        }
        return entry;
    }
    protected void deleteEntry(BasicPoolEntry entry) {
        HttpRoute route = entry.getPlannedRoute();
        if (log.isDebugEnabled()) {
            log.debug("Deleting connection" 
                    + " [" + route + "][" + entry.getState() + "]");
        }
        poolLock.lock();
        try {
            closeConnection(entry.getConnection());
            RouteSpecificPool rospl = getRoutePool(route, true);
            rospl.deleteEntry(entry);
            numConnections--;
            if (rospl.isUnused()) {
                routeToPool.remove(route);
            }
            idleConnHandler.remove(entry.getConnection());
        } finally {
            poolLock.unlock();
        }
    }
    protected void deleteLeastUsedEntry() {
        try {
            poolLock.lock();
            BasicPoolEntry entry = freeConnections.remove();
            if (entry != null) {
                deleteEntry(entry);
            } else if (log.isDebugEnabled()) {
                log.debug("No free connection to delete.");
            }
        } finally {
            poolLock.unlock();
        }
    }
    @Override
    protected void handleLostEntry(HttpRoute route) {
        poolLock.lock();
        try {
            RouteSpecificPool rospl = getRoutePool(route, true);
            rospl.dropEntry();
            if (rospl.isUnused()) {
                routeToPool.remove(route);
            }
            numConnections--;
            notifyWaitingThread(rospl);
        } finally {
            poolLock.unlock();
        }
    }
    protected void notifyWaitingThread(RouteSpecificPool rospl) {
        WaitingThread waitingThread = null;
        poolLock.lock();
        try {
            if ((rospl != null) && rospl.hasThread()) {
                if (log.isDebugEnabled()) {
                    log.debug("Notifying thread waiting on pool" +
                            " [" + rospl.getRoute() + "]");
                }
                waitingThread = rospl.nextThread();
            } else if (!waitingThreads.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Notifying thread waiting on any pool");
                }
                waitingThread = waitingThreads.remove();
            } else if (log.isDebugEnabled()) {
                log.debug("Notifying no-one, there are no waiting threads");
            }
            if (waitingThread != null) {
                waitingThread.wakeup();
            }
        } finally {
            poolLock.unlock();
        }
    }
    @Override
    public void deleteClosedConnections() {
        poolLock.lock();
        try {
            Iterator<BasicPoolEntry>  iter = freeConnections.iterator();
            while (iter.hasNext()) {
                BasicPoolEntry entry = iter.next();
                if (!entry.getConnection().isOpen()) {
                    iter.remove();
                    deleteEntry(entry);
                }
            }
        } finally {
            poolLock.unlock();
        }
    }
    @Override
    public void shutdown() {
        poolLock.lock();
        try {
            super.shutdown();
            Iterator<BasicPoolEntry> ibpe = freeConnections.iterator();
            while (ibpe.hasNext()) {
                BasicPoolEntry entry = ibpe.next();
                ibpe.remove();
                closeConnection(entry.getConnection());
            }
            Iterator<WaitingThread> iwth = waitingThreads.iterator();
            while (iwth.hasNext()) {
                WaitingThread waiter = iwth.next();
                iwth.remove();
                waiter.wakeup();
            }
            routeToPool.clear();
        } finally {
            poolLock.unlock();
        }
    }
} 
