abstract class AsynchronousChannelGroupImpl
    extends AsynchronousChannelGroup implements Executor
{
    private static final int internalThreadCount = AccessController.doPrivileged(
        new GetIntegerAction("sun.nio.ch.internalThreadPoolSize", 1));
    private final ThreadPool pool;
    private final AtomicInteger threadCount = new AtomicInteger();
    private ScheduledThreadPoolExecutor timeoutExecutor;
    private final Queue<Runnable> taskQueue;
    private final AtomicBoolean shutdown = new AtomicBoolean();
    private final Object shutdownNowLock = new Object();
    private volatile boolean terminateInitiated;
    AsynchronousChannelGroupImpl(AsynchronousChannelProvider provider,
                                 ThreadPool pool)
    {
        super(provider);
        this.pool = pool;
        if (pool.isFixedThreadPool()) {
            taskQueue = new ConcurrentLinkedQueue<Runnable>();
        } else {
            taskQueue = null;   
        }
        this.timeoutExecutor = (ScheduledThreadPoolExecutor)
            Executors.newScheduledThreadPool(1, ThreadPool.defaultThreadFactory());
        this.timeoutExecutor.setRemoveOnCancelPolicy(true);
    }
    final ExecutorService executor() {
        return pool.executor();
    }
    final boolean isFixedThreadPool() {
        return pool.isFixedThreadPool();
    }
    final int fixedThreadCount() {
        if (isFixedThreadPool()) {
            return pool.poolSize();
        } else {
            return pool.poolSize() + internalThreadCount;
        }
    }
    private Runnable bindToGroup(final Runnable task) {
        final AsynchronousChannelGroupImpl thisGroup = this;
        return new Runnable() {
            public void run() {
                Invoker.bindToGroup(thisGroup);
                task.run();
            }
        };
    }
    private void startInternalThread(final Runnable task) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                ThreadPool.defaultThreadFactory().newThread(task).start();
                return null;
            }
         });
    }
    protected final void startThreads(Runnable task) {
        if (!isFixedThreadPool()) {
            for (int i=0; i<internalThreadCount; i++) {
                startInternalThread(task);
                threadCount.incrementAndGet();
            }
        }
        if (pool.poolSize() > 0) {
            task = bindToGroup(task);
            try {
                for (int i=0; i<pool.poolSize(); i++) {
                    pool.executor().execute(task);
                    threadCount.incrementAndGet();
                }
            } catch (RejectedExecutionException  x) {
            }
        }
    }
    final int threadCount() {
        return threadCount.get();
    }
    final int threadExit(Runnable task, boolean replaceMe) {
        if (replaceMe) {
            try {
                if (Invoker.isBoundToAnyGroup()) {
                    pool.executor().execute(bindToGroup(task));
                } else {
                    startInternalThread(task);
                }
                return threadCount.get();
            } catch (RejectedExecutionException x) {
            }
        }
        return threadCount.decrementAndGet();
    }
    abstract void executeOnHandlerTask(Runnable task);
    final void executeOnPooledThread(Runnable task) {
        if (isFixedThreadPool()) {
            executeOnHandlerTask(task);
        } else {
            pool.executor().execute(bindToGroup(task));
        }
    }
    final void offerTask(Runnable task) {
        taskQueue.offer(task);
    }
    final Runnable pollTask() {
        return (taskQueue == null) ? null : taskQueue.poll();
    }
    final Future<?> schedule(Runnable task, long timeout, TimeUnit unit) {
        try {
            return timeoutExecutor.schedule(task, timeout, unit);
        } catch (RejectedExecutionException rej) {
            if (terminateInitiated) {
                return null;
            }
            throw new AssertionError(rej);
        }
    }
    @Override
    public final boolean isShutdown() {
        return shutdown.get();
    }
    @Override
    public final boolean isTerminated()  {
        return pool.executor().isTerminated();
    }
    abstract boolean isEmpty();
    abstract Object attachForeignChannel(Channel channel, FileDescriptor fdo)
        throws IOException;
    abstract void detachForeignChannel(Object key);
    abstract void closeAllChannels() throws IOException;
    abstract void shutdownHandlerTasks();
    private void shutdownExecutors() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                pool.executor().shutdown();
                timeoutExecutor.shutdown();
                return null;
            }
        });
    }
    @Override
    public final void shutdown() {
        if (shutdown.getAndSet(true)) {
            return;
        }
        if (!isEmpty()) {
            return;
        }
        synchronized (shutdownNowLock) {
            if (!terminateInitiated) {
                terminateInitiated = true;
                shutdownHandlerTasks();
                shutdownExecutors();
            }
        }
    }
    @Override
    public final void shutdownNow() throws IOException {
        shutdown.set(true);
        synchronized (shutdownNowLock) {
            if (!terminateInitiated) {
                terminateInitiated = true;
                closeAllChannels();
                shutdownHandlerTasks();
                shutdownExecutors();
            }
        }
    }
    final void detachFromThreadPool() {
        if (shutdown.getAndSet(true))
            throw new AssertionError("Already shutdown");
        if (!isEmpty())
            throw new AssertionError("Group not empty");
        shutdownHandlerTasks();
    }
    @Override
    public final boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException
    {
        return pool.executor().awaitTermination(timeout, unit);
    }
    @Override
    public final void execute(Runnable task) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            final AccessControlContext acc = AccessController.getContext();
            final Runnable delegate = task;
            task = new Runnable() {
                @Override
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        @Override
                        public Void run() {
                            delegate.run();
                            return null;
                        }
                    }, acc);
                }
            };
        }
        executeOnPooledThread(task);
    }
}
