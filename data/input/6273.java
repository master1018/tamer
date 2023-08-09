public class SolarisAsynchronousChannelProvider
    extends AsynchronousChannelProvider
{
    private static volatile SolarisEventPort defaultEventPort;
    private SolarisEventPort defaultEventPort() throws IOException {
        if (defaultEventPort == null) {
            synchronized (SolarisAsynchronousChannelProvider.class) {
                if (defaultEventPort == null) {
                    defaultEventPort =
                        new SolarisEventPort(this, ThreadPool.getDefault()).start();
                }
            }
        }
        return defaultEventPort;
    }
    public SolarisAsynchronousChannelProvider() {
    }
    @Override
    public AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory factory)
        throws IOException
    {
        return new SolarisEventPort(this, ThreadPool.create(nThreads, factory)).start();
    }
    @Override
    public AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize)
        throws IOException
    {
        return new SolarisEventPort(this, ThreadPool.wrap(executor, initialSize)).start();
    }
    private SolarisEventPort toEventPort(AsynchronousChannelGroup group)
        throws IOException
    {
        if (group == null) {
            return defaultEventPort();
        } else {
            if (!(group instanceof SolarisEventPort))
                throw new IllegalChannelGroupException();
            return (SolarisEventPort)group;
        }
    }
    @Override
    public AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(AsynchronousChannelGroup group)
        throws IOException
    {
        return new UnixAsynchronousServerSocketChannelImpl(toEventPort(group));
    }
    @Override
    public AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup group)
        throws IOException
    {
        return new UnixAsynchronousSocketChannelImpl(toEventPort(group));
    }
}
