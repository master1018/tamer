final class DGCImpl implements DGC {
    static final Log dgcLog = Log.getLog("sun.rmi.dgc", "dgc",
        LogStream.parseLevel(AccessController.doPrivileged(
            new GetPropertyAction("sun.rmi.dgc.logLevel"))));
    private static final long leaseValue =              
        AccessController.doPrivileged(
            new GetLongAction("java.rmi.dgc.leaseValue", 600000));
    private static final long leaseCheckInterval =
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.dgc.checkInterval", leaseValue / 2));
    private static final ScheduledExecutorService scheduler =
        AccessController.doPrivileged(
            new RuntimeUtil.GetInstanceAction()).getScheduler();
    private static DGCImpl dgc;
    private Map<VMID,LeaseInfo> leaseTable = new HashMap<VMID,LeaseInfo>();
    private Future<?> checker = null;
    static DGCImpl getDGCImpl() {
        return dgc;
    }
    private DGCImpl() {}
    public Lease dirty(ObjID[] ids, long sequenceNum, Lease lease) {
        VMID vmid = lease.getVMID();
        long duration = leaseValue;
        if (dgcLog.isLoggable(Log.VERBOSE)) {
            dgcLog.log(Log.VERBOSE, "vmid = " + vmid);
        }
        if (vmid == null) {
            vmid = new VMID();
            if (dgcLog.isLoggable(Log.BRIEF)) {
                String clientHost;
                try {
                    clientHost = RemoteServer.getClientHost();
                } catch (ServerNotActiveException e) {
                    clientHost = "<unknown host>";
                }
                dgcLog.log(Log.BRIEF, " assigning vmid " + vmid +
                           " to client " + clientHost);
            }
        }
        lease = new Lease(vmid, duration);
        synchronized (leaseTable) {
            LeaseInfo info = leaseTable.get(vmid);
            if (info == null) {
                leaseTable.put(vmid, new LeaseInfo(vmid, duration));
                if (checker == null) {
                    checker = scheduler.scheduleWithFixedDelay(
                        new Runnable() {
                            public void run() {
                                checkLeases();
                            }
                        },
                        leaseCheckInterval,
                        leaseCheckInterval, TimeUnit.MILLISECONDS);
                }
            } else {
                info.renew(duration);
            }
        }
        for (ObjID id : ids) {
            if (dgcLog.isLoggable(Log.VERBOSE)) {
                dgcLog.log(Log.VERBOSE, "id = " + id +
                           ", vmid = " + vmid + ", duration = " + duration);
            }
            ObjectTable.referenced(id, sequenceNum, vmid);
        }
        return lease;
    }
    public void clean(ObjID[] ids, long sequenceNum, VMID vmid, boolean strong)
    {
        for (ObjID id : ids) {
            if (dgcLog.isLoggable(Log.VERBOSE)) {
                dgcLog.log(Log.VERBOSE, "id = " + id +
                    ", vmid = " + vmid + ", strong = " + strong);
            }
            ObjectTable.unreferenced(id, sequenceNum, vmid, strong);
        }
    }
    void registerTarget(VMID vmid, Target target) {
        synchronized (leaseTable) {
            LeaseInfo info = leaseTable.get(vmid);
            if (info == null) {
                target.vmidDead(vmid);
            } else {
                info.notifySet.add(target);
            }
        }
    }
    void unregisterTarget(VMID vmid, Target target) {
        synchronized (leaseTable) {
            LeaseInfo info = leaseTable.get(vmid);
            if (info != null) {
                info.notifySet.remove(target);
            }
        }
    }
    private void checkLeases() {
        long time = System.currentTimeMillis();
        List<LeaseInfo> toUnregister = new ArrayList<LeaseInfo>();
        synchronized (leaseTable) {
            Iterator<LeaseInfo> iter = leaseTable.values().iterator();
            while (iter.hasNext()) {
                LeaseInfo info = iter.next();
                if (info.expired(time)) {
                    toUnregister.add(info);
                    iter.remove();
                }
            }
            if (leaseTable.isEmpty()) {
                checker.cancel(false);
                checker = null;
            }
        }
        for (LeaseInfo info : toUnregister) {
            for (Target target : info.notifySet) {
                target.vmidDead(info.vmid);
            }
        }
    }
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                ClassLoader savedCcl =
                    Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(
                        ClassLoader.getSystemClassLoader());
                    try {
                        dgc = new DGCImpl();
                        ObjID dgcID = new ObjID(ObjID.DGC_ID);
                        LiveRef ref = new LiveRef(dgcID, 0);
                        UnicastServerRef disp = new UnicastServerRef(ref);
                        Remote stub =
                            Util.createProxy(DGCImpl.class,
                                             new UnicastRef(ref), true);
                        disp.setSkeleton(dgc);
                        Target target =
                            new Target(dgc, disp, stub, dgcID, true);
                        ObjectTable.putTarget(target);
                    } catch (RemoteException e) {
                        throw new Error(
                            "exception initializing server-side DGC", e);
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(savedCcl);
                }
                return null;
            }
        });
    }
    private static class LeaseInfo {
        VMID vmid;
        long expiration;
        Set<Target> notifySet = new HashSet<Target>();
        LeaseInfo(VMID vmid, long lease) {
            this.vmid = vmid;
            expiration = System.currentTimeMillis() + lease;
        }
        synchronized void renew(long lease) {
            long newExpiration = System.currentTimeMillis() + lease;
            if (newExpiration > expiration)
                expiration = newExpiration;
        }
        boolean expired(long time) {
            if (expiration < time) {
                if (dgcLog.isLoggable(Log.BRIEF)) {
                    dgcLog.log(Log.BRIEF, vmid.toString());
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
