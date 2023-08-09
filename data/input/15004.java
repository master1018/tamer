class SocketChannelImpl
    extends SocketChannel
    implements SelChImpl
{
    private static NativeDispatcher nd;
    private final FileDescriptor fd;
    private final int fdVal;
    private volatile long readerThread = 0;
    private volatile long writerThread = 0;
    private final Object readLock = new Object();
    private final Object writeLock = new Object();
    private final Object stateLock = new Object();
    private static final int ST_UNINITIALIZED = -1;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_PENDING = 1;
    private static final int ST_CONNECTED = 2;
    private static final int ST_KILLPENDING = 3;
    private static final int ST_KILLED = 4;
    private int state = ST_UNINITIALIZED;
    private SocketAddress localAddress;
    private SocketAddress remoteAddress;
    private boolean isInputOpen = true;
    private boolean isOutputOpen = true;
    private boolean readyToConnect = false;
    private Socket socket;
    SocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.fd = Net.socket(true);
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_UNCONNECTED;
    }
    SocketChannelImpl(SelectorProvider sp,
                      FileDescriptor fd,
                      boolean bound)
        throws IOException
    {
        super(sp);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_UNCONNECTED;
        if (bound)
            this.localAddress = Net.localAddress(fd);
    }
    SocketChannelImpl(SelectorProvider sp,
                      FileDescriptor fd, InetSocketAddress remote)
        throws IOException
    {
        super(sp);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_CONNECTED;
        this.localAddress = Net.localAddress(fd);
        this.remoteAddress = remote;
    }
    public Socket socket() {
        synchronized (stateLock) {
            if (socket == null)
                socket = SocketAdaptor.create(this);
            return socket;
        }
    }
    @Override
    public SocketAddress getLocalAddress() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            return localAddress;
        }
    }
    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            return remoteAddress;
        }
    }
    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value)
        throws IOException
    {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (name == StandardSocketOptions.IP_TOS) {
                if (!Net.isIPv6Available())
                    Net.setSocketOption(fd, StandardProtocolFamily.INET, name, value);
                return this;
            }
            Net.setSocketOption(fd, Net.UNSPEC, name, value);
            return this;
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(SocketOption<T> name)
        throws IOException
    {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (name == StandardSocketOptions.IP_TOS) {
                return (Net.isIPv6Available()) ? (T) Integer.valueOf(0) :
                    (T) Net.getSocketOption(fd, StandardProtocolFamily.INET, name);
            }
            return (T) Net.getSocketOption(fd, Net.UNSPEC, name);
        }
    }
    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = defaultOptions();
        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet<SocketOption<?>>(8);
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_KEEPALIVE);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            set.add(StandardSocketOptions.SO_LINGER);
            set.add(StandardSocketOptions.TCP_NODELAY);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(ExtendedSocketOption.SO_OOBINLINE);
            return Collections.unmodifiableSet(set);
        }
    }
    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }
    private boolean ensureReadOpen() throws ClosedChannelException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isConnected())
                throw new NotYetConnectedException();
            if (!isInputOpen)
                return false;
            else
                return true;
        }
    }
    private void ensureWriteOpen() throws ClosedChannelException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isOutputOpen)
                throw new ClosedChannelException();
            if (!isConnected())
                throw new NotYetConnectedException();
        }
    }
    private void readerCleanup() throws IOException {
        synchronized (stateLock) {
            readerThread = 0;
            if (state == ST_KILLPENDING)
                kill();
        }
    }
    private void writerCleanup() throws IOException {
        synchronized (stateLock) {
            writerThread = 0;
            if (state == ST_KILLPENDING)
                kill();
        }
    }
    public int read(ByteBuffer buf) throws IOException {
        if (buf == null)
            throw new NullPointerException();
        synchronized (readLock) {
            if (!ensureReadOpen())
                return -1;
            int n = 0;
            try {
                begin();
                synchronized (stateLock) {
                    if (!isOpen()) {
                        return 0;
                    }
                    readerThread = NativeThread.current();
                }
                for (;;) {
                    n = IOUtil.read(fd, buf, -1, nd, readLock);
                    if ((n == IOStatus.INTERRUPTED) && isOpen()) {
                        continue;
                    }
                    return IOStatus.normalize(n);
                }
            } finally {
                readerCleanup();        
                end(n > 0 || (n == IOStatus.UNAVAILABLE));
                synchronized (stateLock) {
                    if ((n <= 0) && (!isInputOpen))
                        return IOStatus.EOF;
                }
                assert IOStatus.check(n);
            }
        }
    }
    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException
    {
        if ((offset < 0) || (length < 0) || (offset > dsts.length - length))
            throw new IndexOutOfBoundsException();
        synchronized (readLock) {
            if (!ensureReadOpen())
                return -1;
            long n = 0;
            try {
                begin();
                synchronized (stateLock) {
                    if (!isOpen())
                        return 0;
                    readerThread = NativeThread.current();
                }
                for (;;) {
                    n = IOUtil.read(fd, dsts, offset, length, nd);
                    if ((n == IOStatus.INTERRUPTED) && isOpen())
                        continue;
                    return IOStatus.normalize(n);
                }
            } finally {
                readerCleanup();
                end(n > 0 || (n == IOStatus.UNAVAILABLE));
                synchronized (stateLock) {
                    if ((n <= 0) && (!isInputOpen))
                        return IOStatus.EOF;
                }
                assert IOStatus.check(n);
            }
        }
    }
    public int write(ByteBuffer buf) throws IOException {
        if (buf == null)
            throw new NullPointerException();
        synchronized (writeLock) {
            ensureWriteOpen();
            int n = 0;
            try {
                begin();
                synchronized (stateLock) {
                    if (!isOpen())
                        return 0;
                    writerThread = NativeThread.current();
                }
                for (;;) {
                    n = IOUtil.write(fd, buf, -1, nd, writeLock);
                    if ((n == IOStatus.INTERRUPTED) && isOpen())
                        continue;
                    return IOStatus.normalize(n);
                }
            } finally {
                writerCleanup();
                end(n > 0 || (n == IOStatus.UNAVAILABLE));
                synchronized (stateLock) {
                    if ((n <= 0) && (!isOutputOpen))
                        throw new AsynchronousCloseException();
                }
                assert IOStatus.check(n);
            }
        }
    }
    public long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException
    {
        if ((offset < 0) || (length < 0) || (offset > srcs.length - length))
            throw new IndexOutOfBoundsException();
        synchronized (writeLock) {
            ensureWriteOpen();
            long n = 0;
            try {
                begin();
                synchronized (stateLock) {
                    if (!isOpen())
                        return 0;
                    writerThread = NativeThread.current();
                }
                for (;;) {
                    n = IOUtil.write(fd, srcs, offset, length, nd);
                    if ((n == IOStatus.INTERRUPTED) && isOpen())
                        continue;
                    return IOStatus.normalize(n);
                }
            } finally {
                writerCleanup();
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                synchronized (stateLock) {
                    if ((n <= 0) && (!isOutputOpen))
                        throw new AsynchronousCloseException();
                }
                assert IOStatus.check(n);
            }
        }
    }
    int sendOutOfBandData(byte b) throws IOException {
        synchronized (writeLock) {
            ensureWriteOpen();
            int n = 0;
            try {
                begin();
                synchronized (stateLock) {
                    if (!isOpen())
                        return 0;
                    writerThread = NativeThread.current();
                }
                for (;;) {
                    n = sendOutOfBandData(fd, b);
                    if ((n == IOStatus.INTERRUPTED) && isOpen())
                        continue;
                    return IOStatus.normalize(n);
                }
            } finally {
                writerCleanup();
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                synchronized (stateLock) {
                    if ((n <= 0) && (!isOutputOpen))
                        throw new AsynchronousCloseException();
                }
                assert IOStatus.check(n);
            }
        }
    }
    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(fd, block);
    }
    public SocketAddress localAddress() {
        synchronized (stateLock) {
            return localAddress;
        }
    }
    public SocketAddress remoteAddress() {
        synchronized (stateLock) {
            return remoteAddress;
        }
    }
    @Override
    public SocketChannel bind(SocketAddress local) throws IOException {
        synchronized (readLock) {
            synchronized (writeLock) {
                synchronized (stateLock) {
                    if (!isOpen())
                        throw new ClosedChannelException();
                    if (state == ST_PENDING)
                        throw new ConnectionPendingException();
                    if (localAddress != null)
                        throw new AlreadyBoundException();
                    InetSocketAddress isa = (local == null) ?
                        new InetSocketAddress(0) : Net.checkAddress(local);
                    NetHooks.beforeTcpBind(fd, isa.getAddress(), isa.getPort());
                    Net.bind(fd, isa.getAddress(), isa.getPort());
                    localAddress = Net.localAddress(fd);
                }
            }
        }
        return this;
    }
    public boolean isConnected() {
        synchronized (stateLock) {
            return (state == ST_CONNECTED);
        }
    }
    public boolean isConnectionPending() {
        synchronized (stateLock) {
            return (state == ST_PENDING);
        }
    }
    void ensureOpenAndUnconnected() throws IOException { 
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (state == ST_CONNECTED)
                throw new AlreadyConnectedException();
            if (state == ST_PENDING)
                throw new ConnectionPendingException();
        }
    }
    public boolean connect(SocketAddress sa) throws IOException {
        int localPort = 0;
        synchronized (readLock) {
            synchronized (writeLock) {
                ensureOpenAndUnconnected();
                InetSocketAddress isa = Net.checkAddress(sa);
                SecurityManager sm = System.getSecurityManager();
                if (sm != null)
                    sm.checkConnect(isa.getAddress().getHostAddress(),
                                    isa.getPort());
                synchronized (blockingLock()) {
                    int n = 0;
                    try {
                        try {
                            begin();
                            synchronized (stateLock) {
                                if (!isOpen()) {
                                    return false;
                                }
                                if (localAddress == null) {
                                    NetHooks.beforeTcpConnect(fd,
                                                           isa.getAddress(),
                                                           isa.getPort());
                                }
                                readerThread = NativeThread.current();
                            }
                            for (;;) {
                                InetAddress ia = isa.getAddress();
                                if (ia.isAnyLocalAddress())
                                    ia = InetAddress.getLocalHost();
                                n = Net.connect(fd,
                                                ia,
                                                isa.getPort());
                                if (  (n == IOStatus.INTERRUPTED)
                                      && isOpen())
                                    continue;
                                break;
                            }
                            synchronized (stateLock) {
                                if (isOpen() && (localAddress == null) ||
                                    ((InetSocketAddress)localAddress)
                                        .getAddress().isAnyLocalAddress())
                                {
                                    localAddress = Net.localAddress(fd);
                                }
                            }
                        } finally {
                            readerCleanup();
                            end((n > 0) || (n == IOStatus.UNAVAILABLE));
                            assert IOStatus.check(n);
                        }
                    } catch (IOException x) {
                        close();
                        throw x;
                    }
                    synchronized (stateLock) {
                        remoteAddress = isa;
                        if (n > 0) {
                            state = ST_CONNECTED;
                            return true;
                        }
                        if (!isBlocking())
                            state = ST_PENDING;
                        else
                            assert false;
                    }
                }
                return false;
            }
        }
    }
    public boolean finishConnect() throws IOException {
        synchronized (readLock) {
            synchronized (writeLock) {
                synchronized (stateLock) {
                    if (!isOpen())
                        throw new ClosedChannelException();
                    if (state == ST_CONNECTED)
                        return true;
                    if (state != ST_PENDING)
                        throw new NoConnectionPendingException();
                }
                int n = 0;
                try {
                    try {
                        begin();
                        synchronized (blockingLock()) {
                            synchronized (stateLock) {
                                if (!isOpen()) {
                                    return false;
                                }
                                readerThread = NativeThread.current();
                            }
                            if (!isBlocking()) {
                                for (;;) {
                                    n = checkConnect(fd, false,
                                                     readyToConnect);
                                    if (  (n == IOStatus.INTERRUPTED)
                                          && isOpen())
                                        continue;
                                    break;
                                }
                            } else {
                                for (;;) {
                                    n = checkConnect(fd, true,
                                                     readyToConnect);
                                    if (n == 0) {
                                        continue;
                                    }
                                    if (  (n == IOStatus.INTERRUPTED)
                                          && isOpen())
                                        continue;
                                    break;
                                }
                            }
                        }
                    } finally {
                        synchronized (stateLock) {
                            readerThread = 0;
                            if (state == ST_KILLPENDING) {
                                kill();
                                n = 0;
                            }
                        }
                        end((n > 0) || (n == IOStatus.UNAVAILABLE));
                        assert IOStatus.check(n);
                    }
                } catch (IOException x) {
                    close();
                    throw x;
                }
                if (n > 0) {
                    synchronized (stateLock) {
                        state = ST_CONNECTED;
                    }
                    return true;
                }
                return false;
            }
        }
    }
    @Override
    public SocketChannel shutdownInput() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isConnected())
                throw new NotYetConnectedException();
            if (isInputOpen) {
                Net.shutdown(fd, Net.SHUT_RD);
                if (readerThread != 0)
                    NativeThread.signal(readerThread);
                isInputOpen = false;
            }
            return this;
        }
    }
    @Override
    public SocketChannel shutdownOutput() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isConnected())
                throw new NotYetConnectedException();
            if (isOutputOpen) {
                Net.shutdown(fd, Net.SHUT_WR);
                if (writerThread != 0)
                    NativeThread.signal(writerThread);
                isOutputOpen = false;
            }
            return this;
        }
    }
    public boolean isInputOpen() {
        synchronized (stateLock) {
            return isInputOpen;
        }
    }
    public boolean isOutputOpen() {
        synchronized (stateLock) {
            return isOutputOpen;
        }
    }
    protected void implCloseSelectableChannel() throws IOException {
        synchronized (stateLock) {
            isInputOpen = false;
            isOutputOpen = false;
            nd.preClose(fd);
            if (readerThread != 0)
                NativeThread.signal(readerThread);
            if (writerThread != 0)
                NativeThread.signal(writerThread);
            if (!isRegistered())
                kill();
        }
    }
    public void kill() throws IOException {
        synchronized (stateLock) {
            if (state == ST_KILLED)
                return;
            if (state == ST_UNINITIALIZED) {
                state = ST_KILLED;
                return;
            }
            assert !isOpen() && !isRegistered();
            if (readerThread == 0 && writerThread == 0) {
                nd.close(fd);
                state = ST_KILLED;
            } else {
                state = ST_KILLPENDING;
            }
        }
    }
    public boolean translateReadyOps(int ops, int initialOps,
                                     SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps(); 
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((ops & PollArrayWrapper.POLLNVAL) != 0) {
            return false;
        }
        if ((ops & (PollArrayWrapper.POLLERR
                    | PollArrayWrapper.POLLHUP)) != 0) {
            newOps = intOps;
            sk.nioReadyOps(newOps);
            readyToConnect = true;
            return (newOps & ~oldOps) != 0;
        }
        if (((ops & PollArrayWrapper.POLLIN) != 0) &&
            ((intOps & SelectionKey.OP_READ) != 0) &&
            (state == ST_CONNECTED))
            newOps |= SelectionKey.OP_READ;
        if (((ops & PollArrayWrapper.POLLCONN) != 0) &&
            ((intOps & SelectionKey.OP_CONNECT) != 0) &&
            ((state == ST_UNCONNECTED) || (state == ST_PENDING))) {
            newOps |= SelectionKey.OP_CONNECT;
            readyToConnect = true;
        }
        if (((ops & PollArrayWrapper.POLLOUT) != 0) &&
            ((intOps & SelectionKey.OP_WRITE) != 0) &&
            (state == ST_CONNECTED))
            newOps |= SelectionKey.OP_WRITE;
        sk.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }
    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }
    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }
    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        int newOps = 0;
        if ((ops & SelectionKey.OP_READ) != 0)
            newOps |= PollArrayWrapper.POLLIN;
        if ((ops & SelectionKey.OP_WRITE) != 0)
            newOps |= PollArrayWrapper.POLLOUT;
        if ((ops & SelectionKey.OP_CONNECT) != 0)
            newOps |= PollArrayWrapper.POLLCONN;
        sk.selector.putEventOps(sk, newOps);
    }
    public FileDescriptor getFD() {
        return fd;
    }
    public int getFDVal() {
        return fdVal;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSuperclass().getName());
        sb.append('[');
        if (!isOpen())
            sb.append("closed");
        else {
            synchronized (stateLock) {
                switch (state) {
                case ST_UNCONNECTED:
                    sb.append("unconnected");
                    break;
                case ST_PENDING:
                    sb.append("connection-pending");
                    break;
                case ST_CONNECTED:
                    sb.append("connected");
                    if (!isInputOpen)
                        sb.append(" ishut");
                    if (!isOutputOpen)
                        sb.append(" oshut");
                    break;
                }
                if (localAddress() != null) {
                    sb.append(" local=");
                    sb.append(localAddress().toString());
                }
                if (remoteAddress() != null) {
                    sb.append(" remote=");
                    sb.append(remoteAddress().toString());
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }
    private static native int checkConnect(FileDescriptor fd,
                                           boolean block, boolean ready)
        throws IOException;
    private static native int sendOutOfBandData(FileDescriptor fd, byte data)
        throws IOException;
    static {
        Util.load();
        nd = new SocketDispatcher();
    }
}
