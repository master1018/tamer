class PendingIoCache {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int addressSize = unsafe.addressSize();
    private static int dependsArch(int value32, int value64) {
        return (addressSize == 4) ? value32 : value64;
    }
    private static final int SIZEOF_OVERLAPPED = dependsArch(20, 32);
    private boolean closed;
    private boolean closePending;
    private final Map<Long,PendingFuture> pendingIoMap =
        new HashMap<Long,PendingFuture>();
    private long[] overlappedCache = new long[4];
    private int overlappedCacheCount = 0;
    PendingIoCache() {
    }
    long add(PendingFuture<?,?> result) {
        synchronized (this) {
            if (closed)
                throw new AssertionError("Should not get here");
            long ov;
            if (overlappedCacheCount > 0) {
                ov = overlappedCache[--overlappedCacheCount];
            } else {
                ov = unsafe.allocateMemory(SIZEOF_OVERLAPPED);
            }
            pendingIoMap.put(ov, result);
            return ov;
        }
    }
    @SuppressWarnings("unchecked")
    <V,A> PendingFuture<V,A> remove(long overlapped) {
        synchronized (this) {
            PendingFuture<V,A> res = pendingIoMap.remove(overlapped);
            if (res != null) {
                if (overlappedCacheCount < overlappedCache.length) {
                    overlappedCache[overlappedCacheCount++] = overlapped;
                } else {
                    unsafe.freeMemory(overlapped);
                }
                if (closePending) {
                    this.notifyAll();
                }
            }
            return res;
        }
    }
    void close() {
        synchronized (this) {
            if (closed)
                return;
            if (!pendingIoMap.isEmpty())
                clearPendingIoMap();
            while (overlappedCacheCount > 0) {
                unsafe.freeMemory( overlappedCache[--overlappedCacheCount] );
            }
            closed = true;
        }
    }
    private void clearPendingIoMap() {
        assert Thread.holdsLock(this);
        closePending = true;
        try {
            this.wait(50);
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
        closePending = false;
        if (pendingIoMap.isEmpty())
            return;
        for (Long ov: pendingIoMap.keySet()) {
            PendingFuture<?,?> result = pendingIoMap.get(ov);
            assert !result.isDone();
            Iocp iocp = (Iocp)((Groupable)result.channel()).group();
            iocp.makeStale(ov);
            final Iocp.ResultHandler rh = (Iocp.ResultHandler)result.getContext();
            Runnable task = new Runnable() {
                public void run() {
                    rh.failed(-1, new AsynchronousCloseException());
                }
            };
            iocp.executeOnPooledThread(task);
        }
        pendingIoMap.clear();
    }
}
