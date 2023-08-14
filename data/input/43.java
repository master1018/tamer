class SolarisEventPort
    extends Port
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int addressSize = unsafe.addressSize();
    private static int dependsArch(int value32, int value64) {
        return (addressSize == 4) ? value32 : value64;
    }
    private static final int SIZEOF_PORT_EVENT  = dependsArch(16, 24);
    private static final int OFFSETOF_EVENTS    = 0;
    private static final int OFFSETOF_SOURCE    = 4;
    private static final int OFFSETOF_OBJECT    = 8;
    private static final short PORT_SOURCE_USER     = 3;
    private static final short PORT_SOURCE_FD       = 4;
    private final int port;
    private boolean closed;
    SolarisEventPort(AsynchronousChannelProvider provider, ThreadPool pool)
        throws IOException
    {
        super(provider, pool);
        this.port = portCreate();
    }
    SolarisEventPort start() {
        startThreads(new EventHandlerTask());
        return this;
    }
    private void implClose() {
        synchronized (this) {
            if (closed)
                return;
            closed = true;
        }
        portClose(port);
    }
    private void wakeup() {
        try {
            portSend(port, 0);
        } catch (IOException x) {
            throw new AssertionError(x);
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
        if (nThreads == 0) {
            implClose();
        } else {
            while (nThreads-- > 0) {
                try {
                    portSend(port, 0);
                } catch (IOException x) {
                    throw new AssertionError(x);
                }
            }
        }
    }
    @Override
    void startPoll(int fd, int events) {
        try {
            portAssociate(port, PORT_SOURCE_FD, fd, events);
        } catch (IOException x) {
            throw new AssertionError();     
        }
    }
    private class EventHandlerTask implements Runnable {
        public void run() {
            Invoker.GroupAndInvokeCount myGroupAndInvokeCount =
                Invoker.getGroupAndInvokeCount();
            final boolean isPooledThread = (myGroupAndInvokeCount != null);
            boolean replaceMe = false;
            long address = unsafe.allocateMemory(SIZEOF_PORT_EVENT);
            try {
                for (;;) {
                    if (isPooledThread)
                        myGroupAndInvokeCount.resetInvokeCount();
                    replaceMe = false;
                    try {
                        portGet(port, address);
                    } catch (IOException x) {
                        x.printStackTrace();
                        return;
                    }
                    short source = unsafe.getShort(address + OFFSETOF_SOURCE);
                    if (source != PORT_SOURCE_FD) {
                        if (source == PORT_SOURCE_USER) {
                            Runnable task = pollTask();
                            if (task == null) {
                                return;
                            }
                            replaceMe = true;
                            task.run();
                        }
                        continue;
                    }
                    int fd = (int)unsafe.getAddress(address + OFFSETOF_OBJECT);
                    int events = unsafe.getInt(address + OFFSETOF_EVENTS);
                    PollableChannel ch;
                    fdToChannelLock.readLock().lock();
                    try {
                        ch = fdToChannel.get(fd);
                    } finally {
                        fdToChannelLock.readLock().unlock();
                    }
                    if (ch != null) {
                        replaceMe = true;
                        ch.onEvent(events, isPooledThread);
                    }
                }
            } finally {
                unsafe.freeMemory(address);
                int remaining = threadExit(this, replaceMe);
                if (remaining == 0 && isShutdown())
                    implClose();
            }
        }
    }
    private static native void init();
    private static native int portCreate() throws IOException;
    private static native void portAssociate(int port, int source, long object,
        int events) throws IOException;
    private static native void portGet(int port, long pe) throws IOException;
    private static native int portGetn(int port, long address, int max)
        throws IOException;
    private static native void portSend(int port, int events) throws IOException;
    private static native void portClose(int port);
    static {
        Util.load();
        init();
    }
}
