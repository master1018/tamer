public class ServerSocketChannelImpl extends ServerSocketChannel implements
        FileDescriptorHandler {
    private static final int SERVER_STATUS_UNINIT = -1;
    private static final int SERVER_STATUS_OPEN = 0;
    private static final int SERVER_STATUS_CLOSED = 1;
    private final FileDescriptor fd;
    private final ServerSocket socket;
    private final SocketImpl impl;
    int status = SERVER_STATUS_UNINIT;
    boolean isBound = false;
    private static class AcceptLock {}
    private final Object acceptLock = new AcceptLock();
    public ServerSocketChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        status = SERVER_STATUS_OPEN;
        fd = new FileDescriptor();
        Platform.getNetworkSystem().createStreamSocket(fd,
                NetUtil.preferIPv4Stack());
        impl = new PlainServerSocketImpl(fd);
        socket = new ServerSocketAdapter(impl, this);
    }
    @SuppressWarnings("unused")
    private ServerSocketChannelImpl() throws IOException {
        super(SelectorProvider.provider());
        status = SERVER_STATUS_OPEN;
        fd = new FileDescriptor();
        impl = new PlainServerSocketImpl(fd);
        socket = new ServerSocketAdapter(impl, this);
        isBound = false;
    }
    public ServerSocket socket() {
        return socket;
    }
    public SocketChannel accept() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!isBound) {
            throw new NotYetBoundException();
        }
        SocketChannel sockChannel = new SocketChannelImpl(SelectorProvider.provider(), false);
        Socket socketGot = sockChannel.socket();
        try {
            begin();
            synchronized (acceptLock) {
                synchronized (blockingLock()) {
                    boolean isBlocking = isBlocking();
                    if (!isBlocking) {
                        int[] tryResult = new int[1];
                        boolean success = Platform.getNetworkSystem().select(
                                new FileDescriptor[] { this.fd },
                                new FileDescriptor[0], 1, 0, 0, tryResult);
                        if (!success || 0 == tryResult[0]) {
                            return null;
                        }
                    }
                    do {
                        try {
                            ((ServerSocketAdapter) socket).accept(socketGot,
                                    (SocketChannelImpl) sockChannel);
                            break;
                        } catch (SocketTimeoutException e) {
                        }
                    } while (isBlocking);
                }
            }
        } finally {
            end(socketGot.isConnected());
        }
        return sockChannel;
    }
    protected void implConfigureBlocking(boolean blockingMode)
            throws IOException {
    }
    synchronized protected void implCloseSelectableChannel() throws IOException {
        status = SERVER_STATUS_CLOSED;
        if (!socket.isClosed()) {
            socket.close();
        }
    }
    public FileDescriptor getFD() {
        return fd;
    }
    private class ServerSocketAdapter extends ServerSocket {
        ServerSocketChannelImpl channelImpl;
        ServerSocketAdapter(SocketImpl impl,
                ServerSocketChannelImpl aChannelImpl) {
            super(impl);
            this.channelImpl = aChannelImpl;
        }
        public void bind(SocketAddress localAddr, int backlog)
                throws IOException {
            super.bind(localAddr, backlog);
            channelImpl.isBound = true;
        }
        public Socket accept() throws IOException {
            if (!isBound) {
                throw new IllegalBlockingModeException();
            }
            SocketChannel sc = channelImpl.accept();
            if (null == sc) {
                throw new IllegalBlockingModeException();
            }
            return sc.socket();
        }
        private Socket accept(Socket aSocket, SocketChannelImpl sockChannel)
                throws IOException {
            boolean connectOK = false;
            try {
                synchronized (this) {
                    super.implAccept(aSocket);
                    sockChannel.setConnected();
                    sockChannel.setBound(true);
                }
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkAccept(aSocket.getInetAddress().getHostAddress(),
                            aSocket.getPort());
                }
                connectOK = true;
            } finally {
                if (!connectOK) {
                    aSocket.close();
                }
            }
            return aSocket;
        }
        public ServerSocketChannel getChannel() {
            return channelImpl;
        }
        public boolean isBound() {
            return channelImpl.isBound;
        }
        public void bind(SocketAddress localAddr) throws IOException {
            super.bind(localAddr);
            channelImpl.isBound = true;
        }
        public void close() throws IOException {
            synchronized (channelImpl) {
                if (channelImpl.isOpen()) {
                    channelImpl.close();
                } else {
                    super.close();
                }
                channelImpl.status = SERVER_STATUS_CLOSED;
            }
        }
    }
}
