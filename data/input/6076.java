public abstract class AsynchronousChannelGroup {
    private final AsynchronousChannelProvider provider;
    protected AsynchronousChannelGroup(AsynchronousChannelProvider provider) {
        this.provider = provider;
    }
    public final AsynchronousChannelProvider provider() {
        return provider;
    }
    public static AsynchronousChannelGroup withFixedThreadPool(int nThreads,
                                                               ThreadFactory threadFactory)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(nThreads, threadFactory);
    }
    public static AsynchronousChannelGroup withCachedThreadPool(ExecutorService executor,
                                                                int initialSize)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(executor, initialSize);
    }
    public static AsynchronousChannelGroup withThreadPool(ExecutorService executor)
        throws IOException
    {
        return AsynchronousChannelProvider.provider()
            .openAsynchronousChannelGroup(executor, 0);
    }
    public abstract boolean isShutdown();
    public abstract boolean isTerminated();
    public abstract void shutdown();
    public abstract void shutdownNow() throws IOException;
    public abstract boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;
}
