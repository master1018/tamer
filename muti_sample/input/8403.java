final class DGCClient {
    private static long nextSequenceNum = Long.MIN_VALUE;
    private static VMID vmid = new VMID();
    private static final long leaseValue =              
        AccessController.doPrivileged(
            new GetLongAction("java.rmi.dgc.leaseValue",
                              600000)).longValue();
    private static final long cleanInterval =           
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.dgc.cleanInterval",
                              180000)).longValue();
    private static final long gcInterval =              
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.dgc.client.gcInterval",
                              3600000)).longValue();
    private static final int dirtyFailureRetries = 5;
    private static final int cleanFailureRetries = 5;
    private static final ObjID[] emptyObjIDArray = new ObjID[0];
    private static final ObjID dgcID = new ObjID(ObjID.DGC_ID);
    private DGCClient() {}
    static void registerRefs(Endpoint ep, List refs) {
        EndpointEntry epEntry;
        do {
            epEntry = EndpointEntry.lookup(ep);
        } while (!epEntry.registerRefs(refs));
    }
    private static synchronized long getNextSequenceNum() {
        return nextSequenceNum++;
    }
    private static long computeRenewTime(long grantTime, long duration) {
        return grantTime + (duration / 2);
    }
    private static class EndpointEntry {
        private Endpoint endpoint;
        private DGC dgc;
        private Map refTable = new HashMap(5);
        private Set invalidRefs = new HashSet(5);
        private boolean removed = false;
        private long renewTime = Long.MAX_VALUE;
        private long expirationTime = Long.MIN_VALUE;
        private int dirtyFailures = 0;
        private long dirtyFailureStartTime;
        private long dirtyFailureDuration;
        private Thread renewCleanThread;
        private boolean interruptible = false;
        private ReferenceQueue refQueue = new ReferenceQueue();
        private Set pendingCleans = new HashSet(5);
        private static Map endpointTable = new HashMap(5);
        private static GC.LatencyRequest gcLatencyRequest = null;
        public static EndpointEntry lookup(Endpoint ep) {
            synchronized (endpointTable) {
                EndpointEntry entry = (EndpointEntry) endpointTable.get(ep);
                if (entry == null) {
                    entry = new EndpointEntry(ep);
                    endpointTable.put(ep, entry);
                    if (gcLatencyRequest == null) {
                        gcLatencyRequest = GC.requestLatency(gcInterval);
                    }
                }
                return entry;
            }
        }
        private EndpointEntry(final Endpoint endpoint) {
            this.endpoint = endpoint;
            try {
                LiveRef dgcRef = new LiveRef(dgcID, endpoint, false);
                dgc = (DGC) Util.createProxy(DGCImpl.class,
                                             new UnicastRef(dgcRef), true);
            } catch (RemoteException e) {
                throw new Error("internal error creating DGC stub");
            }
            renewCleanThread =  AccessController.doPrivileged(
                new NewThreadAction(new RenewCleanThread(),
                                    "RenewClean-" + endpoint, true));
            renewCleanThread.start();
        }
        public boolean registerRefs(List refs) {
            assert !Thread.holdsLock(this);
            Set refsToDirty = null;     
            long sequenceNum;           
            synchronized (this) {
                if (removed) {
                    return false;
                }
                Iterator iter = refs.iterator();
                while (iter.hasNext()) {
                    LiveRef ref = (LiveRef) iter.next();
                    assert ref.getEndpoint().equals(endpoint);
                    RefEntry refEntry = (RefEntry) refTable.get(ref);
                    if (refEntry == null) {
                        LiveRef refClone = (LiveRef) ref.clone();
                        refEntry = new RefEntry(refClone);
                        refTable.put(refClone, refEntry);
                        if (refsToDirty == null) {
                            refsToDirty = new HashSet(5);
                        }
                        refsToDirty.add(refEntry);
                    }
                    refEntry.addInstanceToRefSet(ref);
                }
                if (refsToDirty == null) {
                    return true;
                }
                refsToDirty.addAll(invalidRefs);
                invalidRefs.clear();
                sequenceNum = getNextSequenceNum();
            }
            makeDirtyCall(refsToDirty, sequenceNum);
            return true;
        }
        private void removeRefEntry(RefEntry refEntry) {
            assert Thread.holdsLock(this);
            assert !removed;
            assert refTable.containsKey(refEntry.getRef());
            refTable.remove(refEntry.getRef());
            invalidRefs.remove(refEntry);
            if (refTable.isEmpty()) {
                synchronized (endpointTable) {
                    endpointTable.remove(endpoint);
                    Transport transport = endpoint.getOutboundTransport();
                    transport.free(endpoint);
                    if (endpointTable.isEmpty()) {
                        assert gcLatencyRequest != null;
                        gcLatencyRequest.cancel();
                        gcLatencyRequest = null;
                    }
                    removed = true;
                }
            }
        }
        private void makeDirtyCall(Set refEntries, long sequenceNum) {
            assert !Thread.holdsLock(this);
            ObjID[] ids;
            if (refEntries != null) {
                ids = createObjIDArray(refEntries);
            } else {
                ids = emptyObjIDArray;
            }
            long startTime = System.currentTimeMillis();
            try {
                Lease lease =
                    dgc.dirty(ids, sequenceNum, new Lease(vmid, leaseValue));
                long duration = lease.getValue();
                long newRenewTime = computeRenewTime(startTime, duration);
                long newExpirationTime = startTime + duration;
                synchronized (this) {
                    dirtyFailures = 0;
                    setRenewTime(newRenewTime);
                    expirationTime = newExpirationTime;
                }
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                synchronized (this) {
                    dirtyFailures++;
                    if (dirtyFailures == 1) {
                        dirtyFailureStartTime = startTime;
                        dirtyFailureDuration = endTime - startTime;
                        setRenewTime(endTime);
                    } else {
                        int n = dirtyFailures - 2;
                        if (n == 0) {
                            dirtyFailureDuration =
                                Math.max((dirtyFailureDuration +
                                          (endTime - startTime)) >> 1, 1000);
                        }
                        long newRenewTime =
                            endTime + (dirtyFailureDuration << n);
                        if (newRenewTime < expirationTime ||
                            dirtyFailures < dirtyFailureRetries ||
                            newRenewTime < dirtyFailureStartTime + leaseValue)
                        {
                            setRenewTime(newRenewTime);
                        } else {
                            setRenewTime(Long.MAX_VALUE);
                        }
                    }
                    if (refEntries != null) {
                        invalidRefs.addAll(refEntries);
                        Iterator iter = refEntries.iterator();
                        while (iter.hasNext()) {
                            RefEntry refEntry = (RefEntry) iter.next();
                            refEntry.markDirtyFailed();
                        }
                    }
                    if (renewTime >= expirationTime) {
                        invalidRefs.addAll(refTable.values());
                    }
                }
            }
        }
        private void setRenewTime(long newRenewTime) {
            assert Thread.holdsLock(this);
            if (newRenewTime < renewTime) {
                renewTime = newRenewTime;
                if (interruptible) {
                    AccessController.doPrivileged(
                        new PrivilegedAction<Void>() {
                            public Void run() {
                            renewCleanThread.interrupt();
                            return null;
                        }
                    });
                }
            } else {
                renewTime = newRenewTime;
            }
        }
        private class RenewCleanThread implements Runnable {
            public void run() {
                do {
                    long timeToWait;
                    RefEntry.PhantomLiveRef phantom = null;
                    boolean needRenewal = false;
                    Set refsToDirty = null;
                    long sequenceNum = Long.MIN_VALUE;
                    synchronized (EndpointEntry.this) {
                        long timeUntilRenew =
                            renewTime - System.currentTimeMillis();
                        timeToWait = Math.max(timeUntilRenew, 1);
                        if (!pendingCleans.isEmpty()) {
                            timeToWait = Math.min(timeToWait, cleanInterval);
                        }
                        interruptible = true;
                    }
                    try {
                        phantom = (RefEntry.PhantomLiveRef)
                            refQueue.remove(timeToWait);
                    } catch (InterruptedException e) {
                    }
                    synchronized (EndpointEntry.this) {
                        interruptible = false;
                        Thread.interrupted();   
                        if (phantom != null) {
                            processPhantomRefs(phantom);
                        }
                        long currentTime = System.currentTimeMillis();
                        if (currentTime > renewTime) {
                            needRenewal = true;
                            if (!invalidRefs.isEmpty()) {
                                refsToDirty = invalidRefs;
                                invalidRefs = new HashSet(5);
                            }
                            sequenceNum = getNextSequenceNum();
                        }
                    }
                    if (needRenewal) {
                        makeDirtyCall(refsToDirty, sequenceNum);
                    }
                    if (!pendingCleans.isEmpty()) {
                        makeCleanCalls();
                    }
                } while (!removed || !pendingCleans.isEmpty());
            }
        }
        private void processPhantomRefs(RefEntry.PhantomLiveRef phantom) {
            assert Thread.holdsLock(this);
            Set strongCleans = null;
            Set normalCleans = null;
            do {
                RefEntry refEntry = phantom.getRefEntry();
                refEntry.removeInstanceFromRefSet(phantom);
                if (refEntry.isRefSetEmpty()) {
                    if (refEntry.hasDirtyFailed()) {
                        if (strongCleans == null) {
                            strongCleans = new HashSet(5);
                        }
                        strongCleans.add(refEntry);
                    } else {
                        if (normalCleans == null) {
                            normalCleans = new HashSet(5);
                        }
                        normalCleans.add(refEntry);
                    }
                    removeRefEntry(refEntry);
                }
            } while ((phantom =
                (RefEntry.PhantomLiveRef) refQueue.poll()) != null);
            if (strongCleans != null) {
                pendingCleans.add(
                    new CleanRequest(createObjIDArray(strongCleans),
                                     getNextSequenceNum(), true));
            }
            if (normalCleans != null) {
                pendingCleans.add(
                    new CleanRequest(createObjIDArray(normalCleans),
                                     getNextSequenceNum(), false));
            }
        }
        private static class CleanRequest {
            final ObjID[] objIDs;
            final long sequenceNum;
            final boolean strong;
            int failures = 0;
            CleanRequest(ObjID[] objIDs, long sequenceNum, boolean strong) {
                this.objIDs = objIDs;
                this.sequenceNum = sequenceNum;
                this.strong = strong;
            }
        }
        private void makeCleanCalls() {
            assert !Thread.holdsLock(this);
            Iterator iter = pendingCleans.iterator();
            while (iter.hasNext()) {
                CleanRequest request = (CleanRequest) iter.next();
                try {
                    dgc.clean(request.objIDs, request.sequenceNum, vmid,
                              request.strong);
                    iter.remove();
                } catch (Exception e) {
                    if (++request.failures >= cleanFailureRetries) {
                        iter.remove();
                    }
                }
            }
        }
        private static ObjID[] createObjIDArray(Set refEntries) {
            ObjID[] ids = new ObjID[refEntries.size()];
            Iterator iter = refEntries.iterator();
            for (int i = 0; i < ids.length; i++) {
                ids[i] = ((RefEntry) iter.next()).getRef().getObjID();
            }
            return ids;
        }
        private class RefEntry {
            private LiveRef ref;
            private Set refSet = new HashSet(5);
            private boolean dirtyFailed = false;
            public RefEntry(LiveRef ref) {
                this.ref = ref;
            }
            public LiveRef getRef() {
                return ref;
            }
            public void addInstanceToRefSet(LiveRef ref) {
                assert Thread.holdsLock(EndpointEntry.this);
                assert ref.equals(this.ref);
                refSet.add(new PhantomLiveRef(ref));
            }
            public void removeInstanceFromRefSet(PhantomLiveRef phantom) {
                assert Thread.holdsLock(EndpointEntry.this);
                assert refSet.contains(phantom);
                refSet.remove(phantom);
            }
            public boolean isRefSetEmpty() {
                assert Thread.holdsLock(EndpointEntry.this);
                return refSet.size() == 0;
            }
            public void markDirtyFailed() {
                assert Thread.holdsLock(EndpointEntry.this);
                dirtyFailed = true;
            }
            public boolean hasDirtyFailed() {
                assert Thread.holdsLock(EndpointEntry.this);
                return dirtyFailed;
            }
            private class PhantomLiveRef extends PhantomReference {
                public PhantomLiveRef(LiveRef ref) {
                    super(ref, EndpointEntry.this.refQueue);
                }
                public RefEntry getRefEntry() {
                    return RefEntry.this;
                }
            }
        }
    }
}
