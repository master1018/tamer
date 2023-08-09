class SocketChannelImpl extends SocketChannel implements FileDescriptorHandler {
    private static final int EOF = -1;
    static final INetworkSystem networkSystem = Platform.getNetworkSystem();
    static final int SOCKET_STATUS_UNINIT = EOF;
    static final int SOCKET_STATUS_UNCONNECTED = 0;
    static final int SOCKET_STATUS_PENDING = 1;
    static final int SOCKET_STATUS_CONNECTED = 2;
    static final int SOCKET_STATUS_CLOSED = 3;
    private static final int TIMEOUT_NONBLOCK = 0;
    private static final int TIMEOUT_BLOCK = EOF;
    private static final int HY_SOCK_STEP_START = 0;
    private static final int HY_PORT_SOCKET_STEP_CHECK = 1;
    private static final int CONNECT_SUCCESS = 0;
    FileDescriptor fd;
    private Socket socket = null;
    InetSocketAddress connectAddress = null;
    InetAddress localAddress = null;
    int localPort;
    int status = SOCKET_STATUS_UNINIT;
    volatile boolean isBound = false;
    private static class ReadLock {}
    private final Object readLock = new ReadLock();
    private static class WriteLock {}
    private final Object writeLock = new WriteLock();
    private byte[] connectContext = new byte[392];
    private int trafficClass = 0;
    public SocketChannelImpl(SelectorProvider selectorProvider)
            throws IOException {
        this(selectorProvider, true);
    }
    public SocketChannelImpl(SelectorProvider selectorProvider, boolean connect)
            throws IOException {
        super(selectorProvider);
        fd = new FileDescriptor();
        status = SOCKET_STATUS_UNCONNECTED;
        if (connect) {
            networkSystem.createStreamSocket(fd, true);
        }
    }
    @SuppressWarnings("unused")
    private SocketChannelImpl() {
        super(SelectorProvider.provider());
        fd = new FileDescriptor();
        connectAddress = new InetSocketAddress(0);
        status = SOCKET_STATUS_CONNECTED;
    }
    SocketChannelImpl(Socket aSocket, FileDescriptor aFd) {
        super(SelectorProvider.provider());
        socket = aSocket;
        fd = aFd;
        status = SOCKET_STATUS_UNCONNECTED;
    }
    @Override
    synchronized public Socket socket() {
        if (null == socket) {
            try {
                InetAddress addr = null;
                int port = 0;
                if (connectAddress != null) {
                    addr = connectAddress.getAddress();
                    port = connectAddress.getPort();
                }
                socket = new SocketAdapter(
                        new PlainSocketImpl(fd, localPort, addr, port), this);
            } catch (SocketException e) {
                return null;
            }
        }
        return socket;
    }
    @Override
    synchronized public boolean isConnected() {
        return status == SOCKET_STATUS_CONNECTED;
    }
    synchronized void setConnected() {
        status = SOCKET_STATUS_CONNECTED;
    }
    void setBound(boolean flag) {
        isBound = flag;
    }
    @Override
    synchronized public boolean isConnectionPending() {
        return status == SOCKET_STATUS_PENDING;
    }
    @Override
    public boolean connect(SocketAddress socketAddress) throws IOException {
        checkUnconnected();
        InetSocketAddress inetSocketAddress = validateAddress(socketAddress);
        InetAddress normalAddr = inetSocketAddress.getAddress();
        if (normalAddr.isAnyLocalAddress()) {
            normalAddr = InetAddress.getLocalHost();
        }
        int port = inetSocketAddress.getPort();
        String hostName = normalAddr.getHostName();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkConnect(hostName, port);
        }
        int result = EOF;
        boolean finished = false;
        try {
            if (isBlocking()) {
                begin();
                networkSystem.connect(fd, trafficClass, normalAddr, port);
                result = CONNECT_SUCCESS; 
            } else {
                result = networkSystem.connectWithTimeout(fd, 0, trafficClass,
                        normalAddr, port, HY_SOCK_STEP_START, connectContext);
                if (!this.isBlocking()) {
                    networkSystem.setNonBlocking(fd, true);
                }
            }
            finished = (CONNECT_SUCCESS == result);
            isBound = finished;
        } catch (IOException e) {
            if (e instanceof ConnectException && !isBlocking()) {
                status = SOCKET_STATUS_PENDING;
            } else {
                if (isOpen()) {
                    close();
                    finished = true;
                }
                throw e;
            }
        } finally {
            if (isBlocking()) {
                end(finished);
            }
        }
        localPort = networkSystem.getSocketLocalPort(fd);
        localAddress = networkSystem.getSocketLocalAddress(fd);
        connectAddress = inetSocketAddress;
        synchronized (this) {
            if (isBlocking()) {
                status = (finished ? SOCKET_STATUS_CONNECTED
                        : SOCKET_STATUS_UNCONNECTED);
            } else {
                status = SOCKET_STATUS_PENDING;
            }
        }
        return finished;
    }
    @Override
    public boolean finishConnect() throws IOException {
        synchronized (this) {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            if (status == SOCKET_STATUS_CONNECTED) {
                return true;
            }
            if (status != SOCKET_STATUS_PENDING) {
                throw new NoConnectionPendingException();
            }
        }
        int result = EOF;
        boolean finished = false;
        try {
            begin();
            result = networkSystem.connectWithTimeout(fd,
                    isBlocking() ? -1 : 0, trafficClass, connectAddress
                            .getAddress(), connectAddress.getPort(),
                    HY_PORT_SOCKET_STEP_CHECK, connectContext);
            finished = (result == CONNECT_SUCCESS);
            isBound = finished;
            localAddress = networkSystem.getSocketLocalAddress(fd);
        } catch (ConnectException e) {
            if (isOpen()) {
                close();
                finished = true;
            }
            throw e;
        } finally {
            end(finished);
        }
        synchronized (this) {
            status = (finished ? SOCKET_STATUS_CONNECTED : status);
            isBound = finished;
            if (!isBlocking()) implConfigureBlocking(false);
        }
        return finished;
    }
    @Override
    public int read(ByteBuffer target) throws IOException {
        if (null == target) {
            throw new NullPointerException();
        }
        checkOpenConnected();
        if (!target.hasRemaining()) {
            return 0;
        }
        int readCount;
        if (target.isDirect() || target.hasArray()) {
            readCount = readImpl(target);
            if (readCount > 0) {
                target.position(target.position() + readCount);
            }
        } else {
            ByteBuffer readBuffer = null;
            byte[] readArray = null;
            readArray = new byte[target.remaining()];
            readBuffer = ByteBuffer.wrap(readArray);
            readCount = readImpl(readBuffer);
            if (readCount > 0) {
                target.put(readArray, 0, readCount);
            }
        }
        return readCount;
    }
    @Override
    public long read(ByteBuffer[] targets, int offset, int length)
            throws IOException {
        if (!isIndexValid(targets, offset, length)) {
            throw new IndexOutOfBoundsException();
        }
        checkOpenConnected();
        int totalCount = calculateByteBufferArray(targets, offset, length);
        if (0 == totalCount) {
            return 0;
        }
        byte[] readArray = new byte[totalCount];
        ByteBuffer readBuffer = ByteBuffer.wrap(readArray);
        int readCount;
        readCount = readImpl(readBuffer);
        if (readCount > 0) {
            int left = readCount;
            int index = offset;
            while (left > 0) {
                int putLength = Math.min(targets[index].remaining(), left);
                targets[index].put(readArray, readCount - left, putLength);
                index++;
                left -= putLength;
            }
        }
        return readCount;
    }
    private boolean isIndexValid(ByteBuffer[] targets, int offset, int length) {
        return (length >= 0) && (offset >= 0)
                && ((long) length + (long) offset <= targets.length);
    }
    private int readImpl(ByteBuffer target) throws IOException {
        synchronized (readLock) {
            int readCount = 0;
            try {
                if (isBlocking()) {
                    begin();
                }
                int offset = target.position();
                int length = target.remaining();
                if (target.isDirect()) {
                    int address = AddressUtil.getDirectBufferAddress(target);
                    readCount = networkSystem.readDirect(fd, address + offset,
                            length, (isBlocking() ? TIMEOUT_BLOCK
                                    : TIMEOUT_NONBLOCK));
                } else {
                    byte[] array = target.array();
                    offset += target.arrayOffset();
                    readCount = networkSystem.read(fd, array, offset, length,
                            (isBlocking() ? TIMEOUT_BLOCK : TIMEOUT_NONBLOCK));
                }
                return readCount;
            } finally {
                if (isBlocking()) {
                    end(readCount > 0);
                }
            }
        }
    }
    @Override
    public int write(ByteBuffer source) throws IOException {
        if (null == source) {
            throw new NullPointerException();
        }
        checkOpenConnected();
        if (!source.hasRemaining()) {
            return 0;
        }
        return writeImpl(source);
    }
    @Override
    public long write(ByteBuffer[] sources, int offset, int length)
            throws IOException {
        if (!isIndexValid(sources, offset, length)) {
            throw new IndexOutOfBoundsException();
        }
        checkOpenConnected();
        int count = calculateByteBufferArray(sources, offset, length);
        if (0 == count) {
            return 0;
        }
        ByteBuffer writeBuf = ByteBuffer.allocate(count);
        for (int val = offset; val < length + offset; val++) {
            ByteBuffer source = sources[val];
            int oldPosition = source.position();
            writeBuf.put(source);
            source.position(oldPosition);
        }
        writeBuf.flip();
        int result = writeImpl(writeBuf);
        int val = offset;
        int written = result;
        while (result > 0) {
            ByteBuffer source = sources[val];
            int gap = Math.min(result, source.remaining());
            source.position(source.position() + gap);
            val++;
            result -= gap;
        }
        return written;
    }
    private int calculateByteBufferArray(ByteBuffer[] sources, int offset,
            int length) {
        int sum = 0;
        for (int val = offset; val < offset + length; val++) {
            sum = sum + sources[val].remaining();
        }
        return sum;
    }
    private int writeImpl(ByteBuffer source) throws IOException {
        synchronized (writeLock) {
            if (!source.hasRemaining()) {
                return 0;
            }
            int writeCount = 0;
            try {
                int pos = source.position();
                int length = source.remaining();
                if (isBlocking()) {
                    begin();
                }
                if (source.isDirect()) {
                    int address = AddressUtil.getDirectBufferAddress(source);
                    writeCount = networkSystem.writeDirect(fd, address, pos,
                            length);
                } else if (source.hasArray()) {
                    pos += source.arrayOffset();
                    writeCount = networkSystem.write(fd, source.array(), pos,
                            length);
                } else {
                    byte[] array = new byte[length];
                    source.get(array);
                    writeCount = networkSystem.write(fd, array, 0, length);
                }
                source.position(pos + writeCount);
            } finally {
                if (isBlocking()) {
                    end(writeCount >= 0);
                }
            }
            return writeCount;
        }
    }
    synchronized private void checkOpenConnected()
            throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!isConnected()) {
            throw new NotYetConnectedException();
        }
    }
    synchronized private void checkUnconnected() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (status == SOCKET_STATUS_CONNECTED) {
            throw new AlreadyConnectedException();
        }
        if (status == SOCKET_STATUS_PENDING) {
            throw new ConnectionPendingException();
        }
    }
    static InetSocketAddress validateAddress(SocketAddress socketAddress) {
        if (null == socketAddress) {
            throw new IllegalArgumentException();
        }
        if (!(socketAddress instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        if (inetSocketAddress.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
        return inetSocketAddress;
    }
    public InetAddress getLocalAddress() throws UnknownHostException {
        byte[] any_bytes = { 0, 0, 0, 0 };
        if (!isBound) {
            return InetAddress.getByAddress(any_bytes);
        }
        return localAddress;
    }
    @Override
    synchronized protected void implCloseSelectableChannel() throws IOException {
        if (SOCKET_STATUS_CLOSED != status) {
            status = SOCKET_STATUS_CLOSED;
            if (null != socket && !socket.isClosed()) {
                socket.close();
            } else {
                networkSystem.socketClose(fd);
            }
        }
    }
    @Override
    protected void implConfigureBlocking(boolean blockMode) throws IOException {
        synchronized (blockingLock()) {
            networkSystem.setNonBlocking(fd, !blockMode);
        }
    }
    public FileDescriptor getFD() {
        return fd;
    }
    private static class SocketAdapter extends Socket {
        SocketChannelImpl channel;
        SocketImpl socketImpl;
        SocketAdapter(SocketImpl socketimpl, SocketChannelImpl channel)
                throws SocketException {
            super(socketimpl);
            socketImpl = socketimpl;
            this.channel = channel;
        }
        @Override
        public SocketChannel getChannel() {
            return channel;
        }
        @Override
        public boolean isBound() {
            return channel.isBound;
        }
        @Override
        public boolean isConnected() {
            return channel.isConnected();
        }
        @Override
        public InetAddress getLocalAddress() {
            try {
                return channel.getLocalAddress();
            } catch (UnknownHostException e) {
                return null;
            }
        }
        @Override
        public void connect(SocketAddress remoteAddr, int timeout)
                throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            if (isConnected()) {
                throw new AlreadyConnectedException();
            }
            super.connect(remoteAddr, timeout);
            channel.localAddress = networkSystem.getSocketLocalAddress(channel.fd);
            if (super.isConnected()) {
                channel.setConnected();
                channel.isBound = super.isBound();
            }
        }
        @Override
        public void bind(SocketAddress localAddr) throws IOException {
            if (channel.isConnected()) {
                throw new AlreadyConnectedException();
            }
            if (SocketChannelImpl.SOCKET_STATUS_PENDING == channel.status) {
                throw new ConnectionPendingException();
            }
            super.bind(localAddr);
            channel.isBound = true;
        }
        @Override
        public void close() throws IOException {
            synchronized (channel) {
                if (channel.isOpen()) {
                    channel.close();
                } else {
                    super.close();
                }
                channel.status = SocketChannelImpl.SOCKET_STATUS_CLOSED;
            }
        }
        @Override
        public boolean getReuseAddress() throws SocketException {
            checkOpen();
            return ((Boolean) socketImpl.getOption(SocketOptions.SO_REUSEADDR))
                    .booleanValue();
        }
        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            checkOpen();
            return ((Integer) socketImpl.getOption(SocketOptions.SO_RCVBUF))
                    .intValue();
        }
        @Override
        public synchronized int getSendBufferSize() throws SocketException {
            checkOpen();
            return ((Integer) socketImpl.getOption(SocketOptions.SO_SNDBUF))
                    .intValue();
        }
        @Override
        public synchronized int getSoTimeout() throws SocketException {
            checkOpen();
            return ((Integer) socketImpl.getOption(SocketOptions.SO_TIMEOUT))
                    .intValue();
        }
        @Override
        public int getTrafficClass() throws SocketException {
            checkOpen();
            return ((Number) socketImpl.getOption(SocketOptions.IP_TOS))
                    .intValue();
        }
        @Override
        public boolean getKeepAlive() throws SocketException {
            checkOpen();
            return ((Boolean) socketImpl.getOption(SocketOptions.SO_KEEPALIVE))
                    .booleanValue();
        }
        @Override
        public boolean getOOBInline() throws SocketException {
            checkOpen();
            return ((Boolean) socketImpl.getOption(SocketOptions.SO_OOBINLINE))
                    .booleanValue();
        }
        @Override
        public int getSoLinger() throws SocketException {
            checkOpen();
            return ((Integer) socketImpl.getOption(SocketOptions.SO_LINGER))
                    .intValue();
        }
        @Override
        public boolean getTcpNoDelay() throws SocketException {
            checkOpen();
            return ((Boolean) socketImpl.getOption(SocketOptions.TCP_NODELAY))
                    .booleanValue();
        }
        @Override
        public void setKeepAlive(boolean value) throws SocketException {
            checkOpen();
            socketImpl.setOption(SocketOptions.SO_KEEPALIVE, value ? Boolean.TRUE
                    : Boolean.FALSE);
        }
        @Override
        public void setOOBInline(boolean oobinline) throws SocketException {
            checkOpen();
            socketImpl.setOption(SocketOptions.SO_OOBINLINE, oobinline ? Boolean.TRUE
                    : Boolean.FALSE);
        }
        @Override
        public synchronized void setReceiveBufferSize(int size)
                throws SocketException {
            checkOpen();
            if (size < 1) {
                throw new IllegalArgumentException(Msg.getString("K0035")); 
            }
            socketImpl
                    .setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
        }
        @Override
        public void setReuseAddress(boolean reuse) throws SocketException {
            checkOpen();
            socketImpl.setOption(SocketOptions.SO_REUSEADDR, reuse ? Boolean.TRUE
                    : Boolean.FALSE);
        }
        @Override
        public synchronized void setSendBufferSize(int size) throws SocketException {
            checkOpen();
            if (size < 1) {
                throw new IllegalArgumentException(Msg.getString("K0035")); 
            }
            socketImpl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size));
        }
        @Override
        public void setSoLinger(boolean on, int timeout) throws SocketException {
            checkOpen();
            if (on && timeout < 0) {
                throw new IllegalArgumentException(Msg.getString("K0045")); 
            }
            int val = on ? (65535 < timeout ? 65535 : timeout) : -1;
            socketImpl.setOption(SocketOptions.SO_LINGER, Integer.valueOf(val));
        }
        @Override
        public synchronized void setSoTimeout(int timeout) throws SocketException {
            checkOpen();
            if (timeout < 0) {
                throw new IllegalArgumentException(Msg.getString("K0036")); 
            }
            socketImpl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
        }
        @Override
        public void setTcpNoDelay(boolean on) throws SocketException {
            checkOpen();
            socketImpl.setOption(SocketOptions.TCP_NODELAY, Boolean.valueOf(on));
        }
        @Override
        public void setTrafficClass(int value) throws SocketException {
            checkOpen();
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException();
            }
            socketImpl.setOption(SocketOptions.IP_TOS, Integer.valueOf(value));
        }
        @Override
        public OutputStream getOutputStream() throws IOException {
            if (!channel.isOpen()) {
                throw new SocketException(Messages.getString("nio.00")); 
            }
            if (!channel.isConnected()) {
                throw new SocketException(Messages.getString("nio.01")); 
            }
            if (isOutputShutdown()) {
                throw new SocketException(Messages.getString("nio.02")); 
            }
            return new SocketChannelOutputStream(channel);
        }
        @Override
        public InputStream getInputStream() throws IOException {
            if (!channel.isOpen()) {
                throw new SocketException(Messages.getString("nio.00")); 
            }
            if (!channel.isConnected()) {
                throw new SocketException(Messages.getString("nio.01")); 
            }
            if (isInputShutdown()) {
                throw new SocketException(Messages.getString("nio.03")); 
            }
            return new SocketChannelInputStream(channel);
        }
        private void checkOpen() throws SocketException {
            if (isClosed()) {
                throw new SocketException(Messages.getString("nio.00")); 
            }
        }
        public SocketImpl getImpl() {
            return socketImpl;
        }
    }
    private static class SocketChannelOutputStream extends OutputStream {
        SocketChannel channel;
        public SocketChannelOutputStream(SocketChannel channel) {
            this.channel = channel;
        }
        @Override
        public void close() throws IOException {
            channel.close();
        }
        @Override
        public void write(byte[] buffer, int offset, int count)
                throws IOException {
            if (0 > offset || 0 > count || count + offset > buffer.length) {
                throw new IndexOutOfBoundsException();
            }
            ByteBuffer buf = ByteBuffer.wrap(buffer, offset, count);
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            channel.write(buf);
        }
        @Override
        public void write(int oneByte) throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put(0, (byte) (oneByte & 0xFF));
            channel.write(buffer);
        }
    }
    private static class SocketChannelInputStream extends InputStream {
        SocketChannel channel;
        public SocketChannelInputStream(SocketChannel channel) {
            this.channel = channel;
        }
        @Override
        public void close() throws IOException {
            channel.close();
        }
        @Override
        public int read() throws IOException {
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            ByteBuffer buf = ByteBuffer.allocate(1);
            int result = channel.read(buf);
            return (-1 == result) ? result : buf.get(0) & 0xFF;
        }
        @Override
        public int read(byte[] buffer, int offset, int count)
                throws IOException {
            if (0 > offset || 0 > count || count + offset > buffer.length) {
                throw new IndexOutOfBoundsException();
            }
            if (!channel.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            ByteBuffer buf = ByteBuffer.wrap(buffer, offset, count);
            return channel.read(buf);
        }
    }
}
