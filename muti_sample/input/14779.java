public final class ObjectTable {
    private final static long gcInterval =              
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.dgc.server.gcInterval", 3600000));
    private static final Object tableLock = new Object();
    private static final Map<ObjectEndpoint,Target> objTable =
        new HashMap<ObjectEndpoint,Target>();
    private static final Map<WeakRef,Target> implTable =
        new HashMap<WeakRef,Target>();
    private static final Object keepAliveLock = new Object();
    private static int keepAliveCount = 0;
    private static Thread reaper = null;
    static final ReferenceQueue reapQueue = new ReferenceQueue();
    private static GC.LatencyRequest gcLatencyRequest = null;
    private ObjectTable() {}
    static Target getTarget(ObjectEndpoint oe) {
        synchronized (tableLock) {
            return objTable.get(oe);
        }
    }
    public static Target getTarget(Remote impl) {
        synchronized (tableLock) {
            return implTable.get(new WeakRef(impl));
        }
    }
    public static Remote getStub(Remote impl)
        throws NoSuchObjectException
    {
        Target target = getTarget(impl);
        if (target == null) {
            throw new NoSuchObjectException("object not exported");
        } else {
            return target.getStub();
        }
    }
   public static boolean unexportObject(Remote obj, boolean force)
        throws java.rmi.NoSuchObjectException
    {
        synchronized (tableLock) {
            Target target = getTarget(obj);
            if (target == null) {
                throw new NoSuchObjectException("object not exported");
            } else {
                if (target.unexport(force)) {
                    removeTarget(target);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    static void putTarget(Target target) throws ExportException {
        ObjectEndpoint oe = target.getObjectEndpoint();
        WeakRef weakImpl = target.getWeakImpl();
        if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE)) {
            DGCImpl.dgcLog.log(Log.VERBOSE, "add object " + oe);
        }
        synchronized (tableLock) {
            if (target.getImpl() != null) {
                if (objTable.containsKey(oe)) {
                    throw new ExportException(
                        "internal error: ObjID already in use");
                } else if (implTable.containsKey(weakImpl)) {
                    throw new ExportException("object already exported");
                }
                objTable.put(oe, target);
                implTable.put(weakImpl, target);
                if (!target.isPermanent()) {
                    incrementKeepAliveCount();
                }
            }
        }
    }
    private static void removeTarget(Target target) {
        ObjectEndpoint oe = target.getObjectEndpoint();
        WeakRef weakImpl = target.getWeakImpl();
        if (DGCImpl.dgcLog.isLoggable(Log.VERBOSE)) {
            DGCImpl.dgcLog.log(Log.VERBOSE, "remove object " + oe);
        }
        objTable.remove(oe);
        implTable.remove(weakImpl);
        target.markRemoved();   
    }
    static void referenced(ObjID id, long sequenceNum, VMID vmid) {
        synchronized (tableLock) {
            ObjectEndpoint oe =
                new ObjectEndpoint(id, Transport.currentTransport());
            Target target = objTable.get(oe);
            if (target != null) {
                target.referenced(sequenceNum, vmid);
            }
        }
    }
    static void unreferenced(ObjID id, long sequenceNum, VMID vmid,
                             boolean strong)
    {
        synchronized (tableLock) {
            ObjectEndpoint oe =
                new ObjectEndpoint(id, Transport.currentTransport());
            Target target = objTable.get(oe);
            if (target != null)
                target.unreferenced(sequenceNum, vmid, strong);
        }
    }
    static void incrementKeepAliveCount() {
        synchronized (keepAliveLock) {
            keepAliveCount++;
            if (reaper == null) {
                reaper = AccessController.doPrivileged(
                    new NewThreadAction(new Reaper(), "Reaper", false));
                reaper.start();
            }
            if (gcLatencyRequest == null) {
                gcLatencyRequest = GC.requestLatency(gcInterval);
            }
        }
    }
    static void decrementKeepAliveCount() {
        synchronized (keepAliveLock) {
            keepAliveCount--;
            if (keepAliveCount == 0) {
                if (!(reaper != null)) { throw new AssertionError(); }
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        reaper.interrupt();
                        return null;
                    }
                });
                reaper = null;
                gcLatencyRequest.cancel();
                gcLatencyRequest = null;
            }
        }
    }
    private static class Reaper implements Runnable {
        public void run() {
            try {
                do {
                    WeakRef weakImpl = (WeakRef) reapQueue.remove();
                    synchronized (tableLock) {
                        Target target = implTable.get(weakImpl);
                        if (target != null) {
                            if (!target.isEmpty()) {
                                throw new Error(
                                    "object with known references collected");
                            } else if (target.isPermanent()) {
                                throw new Error("permanent object collected");
                            }
                            removeTarget(target);
                        }
                    }
                } while (!Thread.interrupted());
            } catch (InterruptedException e) {
            }
        }
    }
}
