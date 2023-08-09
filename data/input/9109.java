public class WindowsAsynchronousChannelProvider
    extends AsynchronousChannelProvider
{
    private static volatile Iocp defaultIocp;
    public WindowsAsynchronousChannelProvider() {
    }
    private Iocp defaultIocp() throws IOException {
        if (defaultIocp == null) {
            synchronized (WindowsAsynchronousChannelProvider.class) {
                if (defaultIocp == null) {
                    defaultIocp = new Iocp(this, ThreadPool.getDefault()).start();
                }
            }
        }
        return defaultIocp;
    }
    @Override
    public AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory factory)
        throws IOException
    {
        return new Iocp(this, ThreadPool.create(nThreads, factory)).start();
    }
    @Override
    public AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize)
        throws IOException
    {
        return new Iocp(this, ThreadPool.wrap(executor, initialSize)).start();
    }
    private Iocp toIocp(AsynchronousChannelGroup group) throws IOException {
        if (group == null) {
            return defaultIocp();
        } else {
            if (!(group instanceof Iocp))
                throw new IllegalChannelGroupException();
            return (Iocp)group;
        }
    }
    @Override
    public AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(AsynchronousChannelGroup group)
        throws IOException
    {
        return new WindowsAsynchronousServerSocketChannelImpl(toIocp(group));
    }
    @Override
    public AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup group)
        throws IOException
    {
        return new WindowsAsynchronousSocketChannelImpl(toIocp(group));
    }
}
