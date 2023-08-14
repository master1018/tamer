class Iocp extends AsynchronousChannelGroupImpl {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long INVALID_HANDLE_VALUE  = -1L;
    private static final boolean supportsThreadAgnosticIo;
    private final ReadWriteLock keyToChannelLock = new ReentrantReadWriteLock();
    private final Map<Integer,OverlappedChannel> keyToChannel =
        new HashMap<Integer,OverlappedChannel>();
    private int nextCompletionKey;
    private final long port;
    private boolean closed;
    private final Set<Long> staleIoSet = new HashSet<Long>();
    Iocp(AsynchronousChannelProvider provider, ThreadPool pool)
        throws IOException
    {
        super(provider, pool);
        this.port =
          createIoCompletionPort(INVALID_HANDLE_VALUE, 0, 0, fixedThreadCount());
        this.nextCompletionKey = 1;
    }
    Iocp start() {
        startThreads(new EventHandlerTask());
        return this;
    }
    static interface OverlappedChannel extends Closeable {
        <V,A> PendingFuture<V,A> getByOverlapped(long overlapped);
    }
    static boolean supportsThreadAgnosticIo() {
        return supportsThreadAgnosticIo;
    }
    void implClose() {
        synchronized (this) {
            if (closed)
                return;
            closed = true;
        }
        close0(port);
        synchronized (staleIoSet) {
            for (Long ov: staleIoSet) {
                unsafe.freeMemory(ov);
            }
            staleIoSet.clear();
        }
    }
    @Override
    boolean isEmpty() {
        keyToChannelLock.writeLock().lock();
        try {
            return keyToChannel.isEmpty();
        } finally {
            keyToChannelLock.writeLock().unlock();
        }
    }
    @Override
    final Object attachForeignChannel(final Channel channel, FileDescriptor fdObj)
        throws IOException
    {
        int key = associate(new OverlappedChannel() {
            public <V,A> PendingFuture<V,A> getByOverlapped(long overlapped) {
                return null;
            }
            public void close() throws IOException {
                channel.close();
            }
        }, 0L);
        return Integer.valueOf(key);
    }
    @Override
    final void detachForeignChannel(Object key) {
        disassociate((Integer)key);
    }
    @Override
    void closeAllChannels() {
        final int MAX_BATCH_SIZE = 32;
        OverlappedChannel channels[] = new OverlappedChannel[MAX_BATCH_SIZE];
        int count;
        do {
            keyToChannelLock.writeLock().lock();
            count = 0;
            try {
                for (Integer key: keyToChannel.keySet()) {
                    channels[count++] = keyToChannel.get(key);
                    if (count >= MAX_BATCH_SIZE)
                        break;
                }
            } finally {
                keyToChannelLock.writeLock().unlock();
            }
            for (int i=0; i<count; i++) {
                try {
                    channels[i].close();
                } catch (IOException ignore) { }
            }
        } while (count > 0);
    }
    private void wakeup() {
        try {
            postQueuedCompletionStatus(port, 0);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
    @Override
    void executeOnHandlerTask(Runnable task) {
        synchronized (this) {
            if (closed)
                throw new RejectedExecutionException();
            offerTask(task);
            wakeup();
        }
    }
    @Override
    void shutdownHandlerTasks() {
        int nThreads = threadCount();
        while (nThreads-- > 0) {
            wakeup();
        }
    }
    int associate(OverlappedChannel ch, long handle) throws IOException {
        keyToChannelLock.writeLock().lock();
        int key;
        try {
            if (isShutdown())
                throw new ShutdownChannelGroupException();
            do {
                key = nextCompletionKey++;
            } while ((key == 0) || keyToChannel.containsKey(key));
            if (handle != 0L) {
                createIoCompletionPort(handle, port, key, 0);
            }
            keyToChannel.put(key, ch);
        } finally {
            keyToChannelLock.writeLock().unlock();
        }
        return key;
    }
    void disassociate(int key) {
        boolean checkForShutdown = false;
        keyToChannelLock.writeLock().lock();
        try {
            keyToChannel.remove(key);
            if (keyToChannel.isEmpty())
                checkForShutdown = true;
        } finally {
            keyToChannelLock.writeLock().unlock();
        }
        if (checkForShutdown && isShutdown()) {
            try {
                shutdownNow();
            } catch (IOException ignore) { }
        }
    }
    void makeStale(Long overlapped) {
        synchronized (staleIoSet) {
            staleIoSet.add(overlapped);
        }
    }
    private void checkIfStale(long ov) {
        synchronized (staleIoSet) {
            boolean removed = staleIoSet.remove(ov);
            if (removed) {
                unsafe.freeMemory(ov);
            }
        }
    }
    static interface ResultHandler {
        public void completed(int bytesTransferred, boolean canInvokeDirect);
        public void failed(int error, IOException ioe);
    }
    private static IOException translateErrorToIOException(int error) {
        String msg = getErrorMessage(error);
        if (msg == null)
            msg = "Unknown error: 0x0" + Integer.toHexString(error);
        return new IOException(msg);
    }
    private class EventHandlerTask implements Runnable {
        public void run() {
            Invoker.GroupAndInvokeCount myGroupAndInvokeCount =
                Invoker.getGroupAndInvokeCount();
            boolean canInvokeDirect = (myGroupAndInvokeCount != null);
            CompletionStatus ioResult = new CompletionStatus();
            boolean replaceMe = false;
            try {
                for (;;) {
                    if (myGroupAndInvokeCount != null)
                        myGroupAndInvokeCount.resetInvokeCount();
                    replaceMe = false;
                    try {
                        getQueuedCompletionStatus(port, ioResult);
                    } catch (IOException x) {
                        x.printStackTrace();
                        return;
                    }
                    if (ioResult.completionKey() == 0 &&
                        ioResult.overlapped() == 0L)
                    {
                        Runnable task = pollTask();
                        if (task == null) {
                            return;
                        }
                        replaceMe = true;
                        task.run();
                        continue;
                    }
                    OverlappedChannel ch = null;
                    keyToChannelLock.readLock().lock();
                    try {
                        ch = keyToChannel.get(ioResult.completionKey());
                        if (ch == null) {
                            checkIfStale(ioResult.overlapped());
                            continue;
                        }
                    } finally {
                        keyToChannelLock.readLock().unlock();
                    }
                    PendingFuture<?,?> result = ch.getByOverlapped(ioResult.overlapped());
                    if (result == null) {
                        checkIfStale(ioResult.overlapped());
                        continue;
                    }
                    synchronized (result) {
                        if (result.isDone()) {
                            continue;
                        }
                    }
                    int error = ioResult.error();
                    ResultHandler rh = (ResultHandler)result.getContext();
                    replaceMe = true; 
                    if (error == 0) {
                        rh.completed(ioResult.bytesTransferred(), canInvokeDirect);
                    } else {
                        rh.failed(error, translateErrorToIOException(error));
                    }
                }
            } finally {
                int remaining = threadExit(this, replaceMe);
                if (remaining == 0 && isShutdown()) {
                    implClose();
                }
            }
        }
    }
    private static class CompletionStatus {
        private int error;
        private int bytesTransferred;
        private int completionKey;
        private long overlapped;
        private CompletionStatus() { }
        int error() { return error; }
        int bytesTransferred() { return bytesTransferred; }
        int completionKey() { return completionKey; }
        long overlapped() { return overlapped; }
    }
    private static native void initIDs();
    private static native long createIoCompletionPort(long handle,
        long existingPort, int completionKey, int concurrency) throws IOException;
    private static native void close0(long handle);
    private static native void getQueuedCompletionStatus(long completionPort,
        CompletionStatus status) throws IOException;
    private static native void postQueuedCompletionStatus(long completionPort,
        int completionKey) throws IOException;
    private static native String getErrorMessage(int error);
    static {
        Util.load();
        initIDs();
        String osversion = AccessController.doPrivileged(
            new GetPropertyAction("os.version"));
        String vers[] = osversion.split("\\.");
        supportsThreadAgnosticIo = Integer.parseInt(vers[0]) >= 6;
    }
}
