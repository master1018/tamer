public class SctpChannelImpl extends SctpChannel
    implements SelChImpl
{
    private final FileDescriptor fd;
    private final int fdVal;
    private volatile long receiverThread = 0;
    private volatile long senderThread = 0;
    private final Object receiveLock = new Object();
    private final Object sendLock = new Object();
    private final ThreadLocal<Boolean> receiveInvoked =
        new ThreadLocal<Boolean>() {
             @Override protected Boolean initialValue() {
                 return Boolean.FALSE;
            }
    };
    private final Object stateLock = new Object();
    private enum ChannelState {
        UNINITIALIZED,
        UNCONNECTED,
        PENDING,
        CONNECTED,
        KILLPENDING,
        KILLED,
    }
    private ChannelState state = ChannelState.UNINITIALIZED;
    int port = -1;
    private HashSet<InetSocketAddress> localAddresses = new HashSet<InetSocketAddress>();
    private boolean wildcard; 
    private boolean readyToConnect;
    private boolean isShutdown;
    private Association association;
    private Set<SocketAddress> remoteAddresses = Collections.EMPTY_SET;
    public SctpChannelImpl(SelectorProvider provider) throws IOException {
        super(provider);
        this.fd = SctpNet.socket(true);
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ChannelState.UNCONNECTED;
    }
    public SctpChannelImpl(SelectorProvider provider, FileDescriptor fd)
         throws IOException {
        this(provider, fd, null);
    }
    public SctpChannelImpl(SelectorProvider provider,
                           FileDescriptor fd,
                           Association association)
            throws IOException {
        super(provider);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ChannelState.CONNECTED;
        port = (Net.localAddress(fd)).getPort();
        if (association != null) { 
            this.association = association;
        } else { 
            ByteBuffer buf = Util.getTemporaryDirectBuffer(50);
            try {
                receive(buf, null, null, true);
            } finally {
                Util.releaseTemporaryDirectBuffer(buf);
            }
        }
    }
    @Override
    public SctpChannel bind(SocketAddress local) throws IOException {
        synchronized (receiveLock) {
            synchronized (sendLock) {
                synchronized (stateLock) {
                    ensureOpenAndUnconnected();
                    if (isBound())
                        SctpNet.throwAlreadyBoundException();
                    InetSocketAddress isa = (local == null) ?
                        new InetSocketAddress(0) : Net.checkAddress(local);
                    Net.bind(fd, isa.getAddress(), isa.getPort());
                    InetSocketAddress boundIsa = Net.localAddress(fd);
                    port = boundIsa.getPort();
                    localAddresses.add(isa);
                    if (isa.getAddress().isAnyLocalAddress())
                        wildcard = true;
                }
            }
        }
        return this;
    }
    @Override
    public SctpChannel bindAddress(InetAddress address)
            throws IOException {
        bindUnbindAddress(address, true);
        localAddresses.add(new InetSocketAddress(address, port));
        return this;
    }
    @Override
    public SctpChannel unbindAddress(InetAddress address)
            throws IOException {
        bindUnbindAddress(address, false);
        localAddresses.remove(new InetSocketAddress(address, port));
        return this;
    }
    private SctpChannel bindUnbindAddress(InetAddress address, boolean add)
            throws IOException {
        if (address == null)
            throw new IllegalArgumentException();
        synchronized (receiveLock) {
            synchronized (sendLock) {
                synchronized (stateLock) {
                    if (!isOpen())
                        throw new ClosedChannelException();
                    if (!isBound())
                        throw new NotYetBoundException();
                    if (wildcard)
                        throw new IllegalStateException(
                                "Cannot add or remove addresses from a channel that is bound to the wildcard address");
                    if (address.isAnyLocalAddress())
                        throw new IllegalArgumentException(
                                "Cannot add or remove the wildcard address");
                    if (add) {
                        for (InetSocketAddress addr : localAddresses) {
                            if (addr.getAddress().equals(address)) {
                                SctpNet.throwAlreadyBoundException();
                            }
                        }
                    } else { 
                        if (localAddresses.size() <= 1)
                            throw new IllegalUnbindException("Cannot remove address from a channel with only one address bound");
                        boolean foundAddress = false;
                        for (InetSocketAddress addr : localAddresses) {
                            if (addr.getAddress().equals(address)) {
                                foundAddress = true;
                                break;
                            }
                        }
                        if (!foundAddress )
                            throw new IllegalUnbindException("Cannot remove address from a channel that is not bound to that address");
                    }
                    SctpNet.bindx(fdVal, new InetAddress[]{address}, port, add);
                    if (add)
                        localAddresses.add(new InetSocketAddress(address, port));
                    else {
                        for (InetSocketAddress addr : localAddresses) {
                            if (addr.getAddress().equals(address)) {
                                localAddresses.remove(addr);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return this;
    }
    private boolean isBound() {
        synchronized (stateLock) {
            return port == -1 ? false : true;
        }
    }
    private boolean isConnected() {
        synchronized (stateLock) {
            return (state == ChannelState.CONNECTED);
        }
    }
    private void ensureOpenAndUnconnected() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (isConnected())
                throw new AlreadyConnectedException();
            if (state == ChannelState.PENDING)
                throw new ConnectionPendingException();
        }
    }
    private boolean ensureReceiveOpen() throws ClosedChannelException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isConnected())
                throw new NotYetConnectedException();
            else
                return true;
        }
    }
    private void ensureSendOpen() throws ClosedChannelException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (isShutdown)
                throw new ClosedChannelException();
            if (!isConnected())
                throw new NotYetConnectedException();
        }
    }
    private void receiverCleanup() throws IOException {
        synchronized (stateLock) {
            receiverThread = 0;
            if (state == ChannelState.KILLPENDING)
                kill();
        }
    }
    private void senderCleanup() throws IOException {
        synchronized (stateLock) {
            senderThread = 0;
            if (state == ChannelState.KILLPENDING)
                kill();
        }
    }
    @Override
    public Association association() throws ClosedChannelException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isConnected())
                return null;
            return association;
        }
    }
    @Override
    public boolean connect(SocketAddress endpoint) throws IOException {
        synchronized (receiveLock) {
            synchronized (sendLock) {
                ensureOpenAndUnconnected();
                InetSocketAddress isa = Net.checkAddress(endpoint);
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
                                receiverThread = NativeThread.current();
                            }
                            for (;;) {
                                InetAddress ia = isa.getAddress();
                                if (ia.isAnyLocalAddress())
                                    ia = InetAddress.getLocalHost();
                                n = SctpNet.connect(fdVal, ia, isa.getPort());
                                if (  (n == IOStatus.INTERRUPTED)
                                      && isOpen())
                                    continue;
                                break;
                            }
                        } finally {
                            receiverCleanup();
                            end((n > 0) || (n == IOStatus.UNAVAILABLE));
                            assert IOStatus.check(n);
                        }
                    } catch (IOException x) {
                        close();
                        throw x;
                    }
                    if (n > 0) {
                        synchronized (stateLock) {
                            state = ChannelState.CONNECTED;
                            if (!isBound()) {
                                InetSocketAddress boundIsa =
                                        Net.localAddress(fd);
                                port = boundIsa.getPort();
                            }
                            ByteBuffer buf = Util.getTemporaryDirectBuffer(50);
                            try {
                                receive(buf, null, null, true);
                            } finally {
                                Util.releaseTemporaryDirectBuffer(buf);
                            }
                            try {
                                remoteAddresses = getRemoteAddresses();
                            } catch (IOException unused) {  }
                            return true;
                        }
                    } else  {
                        synchronized (stateLock) {
                            if (!isBlocking())
                                state = ChannelState.PENDING;
                            else
                                assert false;
                        }
                    }
                }
                return false;
            }
        }
    }
    @Override
    public boolean connect(SocketAddress endpoint,
                           int maxOutStreams,
                           int maxInStreams)
            throws IOException {
        ensureOpenAndUnconnected();
        return setOption(SCTP_INIT_MAXSTREAMS, InitMaxStreams.
                create(maxInStreams, maxOutStreams)).connect(endpoint);
    }
    @Override
    public boolean isConnectionPending() {
        synchronized (stateLock) {
            return (state == ChannelState.PENDING);
        }
    }
    @Override
    public boolean finishConnect() throws IOException {
        synchronized (receiveLock) {
            synchronized (sendLock) {
                synchronized (stateLock) {
                    if (!isOpen())
                        throw new ClosedChannelException();
                    if (isConnected())
                        return true;
                    if (state != ChannelState.PENDING)
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
                                receiverThread = NativeThread.current();
                            }
                            if (!isBlocking()) {
                                for (;;) {
                                    n = checkConnect(fd, false, readyToConnect);
                                    if (  (n == IOStatus.INTERRUPTED)
                                          && isOpen())
                                        continue;
                                    break;
                                }
                            } else {
                                for (;;) {
                                    n = checkConnect(fd, true, readyToConnect);
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
                            receiverThread = 0;
                            if (state == ChannelState.KILLPENDING) {
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
                        state = ChannelState.CONNECTED;
                        if (!isBound()) {
                            InetSocketAddress boundIsa =
                                    Net.localAddress(fd);
                            port = boundIsa.getPort();
                        }
                        ByteBuffer buf = Util.getTemporaryDirectBuffer(50);
                        try {
                            receive(buf, null, null, true);
                        } finally {
                            Util.releaseTemporaryDirectBuffer(buf);
                        }
                        try {
                            remoteAddresses = getRemoteAddresses();
                        } catch (IOException unused) {  }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(fd, block);
    }
    @Override
    public void implCloseSelectableChannel() throws IOException {
        synchronized (stateLock) {
            SctpNet.preClose(fdVal);
            if (receiverThread != 0)
                NativeThread.signal(receiverThread);
            if (senderThread != 0)
                NativeThread.signal(senderThread);
            if (!isRegistered())
                kill();
        }
    }
    @Override
    public FileDescriptor getFD() {
        return fd;
    }
    @Override
    public int getFDVal() {
        return fdVal;
    }
    private boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
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
            isConnected())
            newOps |= SelectionKey.OP_READ;
        if (((ops & PollArrayWrapper.POLLCONN) != 0) &&
            ((intOps & SelectionKey.OP_CONNECT) != 0) &&
            ((state == ChannelState.UNCONNECTED) || (state == ChannelState.PENDING))) {
            newOps |= SelectionKey.OP_CONNECT;
            readyToConnect = true;
        }
        if (((ops & PollArrayWrapper.POLLOUT) != 0) &&
            ((intOps & SelectionKey.OP_WRITE) != 0) &&
            isConnected())
            newOps |= SelectionKey.OP_WRITE;
        sk.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }
    @Override
    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }
    @Override
    @SuppressWarnings("all")
    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }
    @Override
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
    @Override
    public void kill() throws IOException {
        synchronized (stateLock) {
            if (state == ChannelState.KILLED)
                return;
            if (state == ChannelState.UNINITIALIZED) {
                state = ChannelState.KILLED;
                return;
            }
            assert !isOpen() && !isRegistered();
            if (receiverThread == 0 && senderThread == 0) {
                SctpNet.close(fdVal);
                state = ChannelState.KILLED;
            } else {
                state = ChannelState.KILLPENDING;
            }
        }
    }
    @Override
    public <T> SctpChannel setOption(SctpSocketOption<T> name, T value)
            throws IOException {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            SctpNet.setSocketOption(fdVal, name, value, 0 );
        }
        return this;
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(SctpSocketOption<T> name) throws IOException {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            return (T)SctpNet.getSocketOption(fdVal, name, 0 );
        }
    }
    private static class DefaultOptionsHolder {
        static final Set<SctpSocketOption<?>> defaultOptions = defaultOptions();
        private static Set<SctpSocketOption<?>> defaultOptions() {
            HashSet<SctpSocketOption<?>> set = new HashSet<SctpSocketOption<?>>(10);
            set.add(SCTP_DISABLE_FRAGMENTS);
            set.add(SCTP_EXPLICIT_COMPLETE);
            set.add(SCTP_FRAGMENT_INTERLEAVE);
            set.add(SCTP_INIT_MAXSTREAMS);
            set.add(SCTP_NODELAY);
            set.add(SCTP_PRIMARY_ADDR);
            set.add(SCTP_SET_PEER_PRIMARY_ADDR);
            set.add(SO_SNDBUF);
            set.add(SO_RCVBUF);
            set.add(SO_LINGER);
            return Collections.unmodifiableSet(set);
        }
    }
    @Override
    public final Set<SctpSocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }
    @Override
    public <T> MessageInfo receive(ByteBuffer buffer,
                                   T attachment,
                                   NotificationHandler<T> handler)
            throws IOException {
        return receive(buffer, attachment, handler, false);
    }
    private <T> MessageInfo receive(ByteBuffer buffer,
                                    T attachment,
                                    NotificationHandler<T> handler,
                                    boolean fromConnect)
            throws IOException {
        if (buffer == null)
            throw new IllegalArgumentException("buffer cannot be null");
        if (buffer.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        if (receiveInvoked.get())
            throw new IllegalReceiveException(
                    "cannot invoke receive from handler");
        receiveInvoked.set(Boolean.TRUE);
        try {
            SctpResultContainer resultContainer = new SctpResultContainer();
            do {
                resultContainer.clear();
                synchronized (receiveLock) {
                    if (!ensureReceiveOpen())
                        return null;
                    int n = 0;
                    try {
                        begin();
                        synchronized (stateLock) {
                            if(!isOpen())
                                return null;
                            receiverThread = NativeThread.current();
                        }
                        do {
                            n = receive(fdVal, buffer, resultContainer, fromConnect);
                        } while ((n == IOStatus.INTERRUPTED) && isOpen());
                    } finally {
                        receiverCleanup();
                        end((n > 0) || (n == IOStatus.UNAVAILABLE));
                        assert IOStatus.check(n);
                    }
                    if (!resultContainer.isNotification()) {
                        if (resultContainer.hasSomething()) {
                            SctpMessageInfoImpl info =
                                    resultContainer.getMessageInfo();
                            synchronized (stateLock) {
                                assert association != null;
                                info.setAssociation(association);
                            }
                            return info;
                        } else
                            return null;
                    } else { 
                        synchronized (stateLock) {
                            handleNotificationInternal(
                                    resultContainer);
                        }
                    }
                    if (fromConnect)  {
                        return null;
                    }
                }  
            } while (handler == null ? true :
                (invokeNotificationHandler(resultContainer, handler, attachment)
                 == HandlerResult.CONTINUE));
            return null;
        } finally {
            receiveInvoked.set(Boolean.FALSE);
        }
    }
    private int receive(int fd,
                        ByteBuffer dst,
                        SctpResultContainer resultContainer,
                        boolean peek)
            throws IOException {
        int pos = dst.position();
        int lim = dst.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (dst instanceof DirectBuffer && rem > 0)
            return receiveIntoNativeBuffer(fd, resultContainer, dst, rem, pos, peek);
        int newSize = Math.max(rem, 1);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(newSize);
        try {
            int n = receiveIntoNativeBuffer(fd, resultContainer, bb, newSize, 0, peek);
            bb.flip();
            if (n > 0 && rem > 0)
                dst.put(bb);
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }
    private int receiveIntoNativeBuffer(int fd,
                                        SctpResultContainer resultContainer,
                                        ByteBuffer bb,
                                        int rem,
                                        int pos,
                                        boolean peek)
        throws IOException
    {
        int n = receive0(fd, resultContainer, ((DirectBuffer)bb).address() + pos, rem, peek);
        if (n > 0)
            bb.position(pos + n);
        return n;
    }
    private InternalNotificationHandler<?> internalNotificationHandler =
            new InternalNotificationHandler();
    private void handleNotificationInternal(SctpResultContainer resultContainer)
    {
        invokeNotificationHandler(resultContainer,
                internalNotificationHandler, null);
    }
    private class InternalNotificationHandler<T>
            extends AbstractNotificationHandler<T>
    {
        @Override
        public HandlerResult handleNotification(
                AssociationChangeNotification not, T unused) {
            if (not.event().equals(
                    AssociationChangeNotification.AssocChangeEvent.COMM_UP) &&
                    association == null) {
                SctpAssocChange sac = (SctpAssocChange) not;
                association = new SctpAssociationImpl
                       (sac.assocId(), sac.maxInStreams(), sac.maxOutStreams());
            }
            return HandlerResult.CONTINUE;
        }
    }
    private <T> HandlerResult invokeNotificationHandler
                                 (SctpResultContainer resultContainer,
                                  NotificationHandler<T> handler,
                                  T attachment) {
        SctpNotification notification = resultContainer.notification();
        synchronized (stateLock) {
            notification.setAssociation(association);
        }
        if (!(handler instanceof AbstractNotificationHandler)) {
            return handler.handleNotification(notification, attachment);
        }
        AbstractNotificationHandler absHandler =
                (AbstractNotificationHandler)handler;
        switch(resultContainer.type()) {
            case ASSOCIATION_CHANGED :
                return absHandler.handleNotification(
                        resultContainer.getAssociationChanged(), attachment);
            case PEER_ADDRESS_CHANGED :
                return absHandler.handleNotification(
                        resultContainer.getPeerAddressChanged(), attachment);
            case SEND_FAILED :
                return absHandler.handleNotification(
                        resultContainer.getSendFailed(), attachment);
            case SHUTDOWN :
                return absHandler.handleNotification(
                        resultContainer.getShutdown(), attachment);
            default :
                return absHandler.handleNotification(
                        resultContainer.notification(), attachment);
        }
    }
    private void checkAssociation(Association sendAssociation) {
        synchronized (stateLock) {
            if (sendAssociation != null && !sendAssociation.equals(association)) {
                throw new IllegalArgumentException(
                        "Cannot send to another association");
            }
        }
    }
    private void checkStreamNumber(int streamNumber) {
        synchronized (stateLock) {
            if (association != null) {
                if (streamNumber < 0 ||
                      streamNumber >= association.maxOutboundStreams())
                    throw new InvalidStreamException();
            }
        }
    }
    @Override
    public int send(ByteBuffer buffer, MessageInfo messageInfo)
            throws IOException {
        if (buffer == null)
            throw new IllegalArgumentException("buffer cannot be null");
        if (messageInfo == null)
            throw new IllegalArgumentException("messageInfo cannot be null");
        checkAssociation(messageInfo.association());
        checkStreamNumber(messageInfo.streamNumber());
        synchronized (sendLock) {
            ensureSendOpen();
            int n = 0;
            try {
                begin();
                synchronized (stateLock) {
                    if(!isOpen())
                        return 0;
                    senderThread = NativeThread.current();
                }
                do {
                    n = send(fdVal, buffer, messageInfo);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return IOStatus.normalize(n);
            } finally {
                senderCleanup();
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                assert IOStatus.check(n);
            }
        }
    }
    private int send(int fd, ByteBuffer src, MessageInfo messageInfo)
            throws IOException {
        int streamNumber = messageInfo.streamNumber();
        SocketAddress target = messageInfo.address();
        boolean unordered = messageInfo.isUnordered();
        int ppid = messageInfo.payloadProtocolID();
        if (src instanceof DirectBuffer)
            return sendFromNativeBuffer(fd, src, target, streamNumber,
                    unordered, ppid);
        int pos = src.position();
        int lim = src.limit();
        assert (pos <= lim && streamNumber >= 0);
        int rem = (pos <= lim ? lim - pos : 0);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = sendFromNativeBuffer(fd, bb, target, streamNumber,
                    unordered, ppid);
            if (n > 0) {
                src.position(pos + n);
            }
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }
    private int sendFromNativeBuffer(int fd,
                                     ByteBuffer bb,
                                     SocketAddress target,
                                     int streamNumber,
                                     boolean unordered,
                                     int ppid)
            throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int written = send0(fd, ((DirectBuffer)bb).address() + pos,
                            rem, target, -1 , streamNumber, unordered, ppid);
        if (written > 0)
            bb.position(pos + written);
        return written;
    }
    @Override
    public SctpChannel shutdown() throws IOException {
        synchronized(stateLock) {
            if (isShutdown)
                return this;
            ensureSendOpen();
            SctpNet.shutdown(fdVal, -1);
            if (senderThread != 0)
                NativeThread.signal(senderThread);
            isShutdown = true;
        }
        return this;
    }
    @Override
    public Set<SocketAddress> getAllLocalAddresses()
            throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isBound())
                return Collections.EMPTY_SET;
            return SctpNet.getLocalAddresses(fdVal);
        }
    }
    @Override
    public Set<SocketAddress> getRemoteAddresses()
            throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isConnected() || isShutdown)
                return Collections.EMPTY_SET;
            try {
                return SctpNet.getRemoteAddresses(fdVal, 0);
            } catch (SocketException unused) {
                return remoteAddresses;
            }
        }
    }
    private static native void initIDs();
    static native int receive0(int fd, SctpResultContainer resultContainer,
            long address, int length, boolean peek) throws IOException;
    static native int send0(int fd, long address, int length,
            SocketAddress target, int assocId, int streamNumber,
            boolean unordered, int ppid) throws IOException;
    private static native int checkConnect(FileDescriptor fd, boolean block,
            boolean ready) throws IOException;
    static {
        Util.load();   
        java.security.AccessController.doPrivileged(
                new sun.security.action.LoadLibraryAction("sctp"));
        initIDs();
    }
}
