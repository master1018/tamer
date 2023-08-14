abstract class AsynchronousServerSocketChannelImpl
    extends AsynchronousServerSocketChannel
    implements Cancellable, Groupable
{
    protected final FileDescriptor fd;
    protected volatile SocketAddress localAddress = null;
    private final Object stateLock = new Object();
    private ReadWriteLock closeLock = new ReentrantReadWriteLock();
    private volatile boolean open = true;
    private volatile boolean acceptKilled;
    AsynchronousServerSocketChannelImpl(AsynchronousChannelGroupImpl group) {
        super(group.provider());
        this.fd = Net.serverSocket(true);
    }
    @Override
    public final boolean isOpen() {
        return open;
    }
    final void begin() throws IOException {
        closeLock.readLock().lock();
        if (!isOpen())
            throw new ClosedChannelException();
    }
    final void end() {
        closeLock.readLock().unlock();
    }
    abstract void implClose() throws IOException;
    @Override
    public final void close() throws IOException {
        closeLock.writeLock().lock();
        try {
            if (!open)
                return;     
            open = false;
        } finally {
            closeLock.writeLock().unlock();
        }
        implClose();
    }
    abstract Future<AsynchronousSocketChannel>
        implAccept(Object attachment,
                   CompletionHandler<AsynchronousSocketChannel,Object> handler);
    @Override
    public final Future<AsynchronousSocketChannel> accept() {
        return implAccept(null, null);
    }
    @Override
    @SuppressWarnings("unchecked")
    public final <A> void accept(A attachment,
                                 CompletionHandler<AsynchronousSocketChannel,? super A> handler)
    {
        if (handler == null)
            throw new NullPointerException("'handler' is null");
        implAccept(attachment, (CompletionHandler<AsynchronousSocketChannel,Object>)handler);
    }
    final boolean isAcceptKilled() {
        return acceptKilled;
    }
    @Override
    public final void onCancel(PendingFuture<?,?> task) {
        acceptKilled = true;
    }
    @Override
    public final AsynchronousServerSocketChannel bind(SocketAddress local, int backlog)
        throws IOException
    {
        InetSocketAddress isa = (local == null) ? new InetSocketAddress(0) :
            Net.checkAddress(local);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkListen(isa.getPort());
        try {
            begin();
            synchronized (stateLock) {
                if (localAddress != null)
                    throw new AlreadyBoundException();
                NetHooks.beforeTcpBind(fd, isa.getAddress(), isa.getPort());
                Net.bind(fd, isa.getAddress(), isa.getPort());
                Net.listen(fd, backlog < 1 ? 50 : backlog);
                localAddress = Net.localAddress(fd);
            }
        } finally {
            end();
        }
        return this;
    }
    @Override
    public final SocketAddress getLocalAddress() throws IOException {
        if (!isOpen())
            throw new ClosedChannelException();
        return localAddress;
    }
    @Override
    public final <T> AsynchronousServerSocketChannel setOption(SocketOption<T> name,
                                                               T value)
        throws IOException
    {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        try {
            begin();
            Net.setSocketOption(fd, Net.UNSPEC, name, value);
            return this;
        } finally {
            end();
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        try {
            begin();
            return (T) Net.getSocketOption(fd, Net.UNSPEC, name);
        } finally {
            end();
        }
    }
    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();
        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet<SocketOption<?>>(2);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            return Collections.unmodifiableSet(set);
        }
    }
    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append('[');
        if (!isOpen())
            sb.append("closed");
        else {
            if (localAddress == null) {
                sb.append("unbound");
            } else {
                sb.append(localAddress.toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
