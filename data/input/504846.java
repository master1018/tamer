class DatagramChannelImpl extends DatagramChannel implements
        FileDescriptorHandler {
    private static final INetworkSystem networkSystem = Platform
            .getNetworkSystem();
    private static final int DEFAULT_TIMEOUT = 1;
    private static final byte[] stubArray = new byte[0];
    private FileDescriptor fd;
    private DatagramSocket socket = null;
    InetSocketAddress connectAddress = null;
    private int localPort;
    boolean connected = false;
    boolean isBound = false;
    private static class ReadLock {}
    private final Object readLock = new ReadLock();
    private static class WriteLock {}
    private final Object writeLock = new WriteLock();
    private int trafficClass = 0;
    protected DatagramChannelImpl(SelectorProvider selectorProvider)
            throws IOException {
        super(selectorProvider);
        fd = new FileDescriptor();
        networkSystem.createDatagramSocket(fd, true);
    }
    @SuppressWarnings("unused")
    private DatagramChannelImpl() {
        super(SelectorProvider.provider());
        fd = new FileDescriptor();
        connectAddress = new InetSocketAddress(0);
    }
    @Override
    synchronized public DatagramSocket socket() {
        if (null == socket) {
            socket = new DatagramSocketAdapter(
                    new PlainDatagramSocketImpl(fd, localPort), this);
        }
        return socket;
    }
    InetAddress getLocalAddress() {
        return networkSystem.getSocketLocalAddress(fd);
    }
    @Override
    synchronized public boolean isConnected() {
        return connected;
    }
    @Override
    synchronized public DatagramChannel connect(SocketAddress address)
            throws IOException {
        checkOpen();
        if (connected) {
            throw new IllegalStateException();
        }
        InetSocketAddress inetSocketAddress = SocketChannelImpl
                .validateAddress(address);
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            if (inetSocketAddress.getAddress().isMulticastAddress()) {
                sm.checkMulticast(inetSocketAddress.getAddress());
            } else {
                sm.checkConnect(inetSocketAddress.getAddress().getHostName(),
                        inetSocketAddress.getPort());
            }
        }
        try {
            begin();
            networkSystem.connectDatagram(fd, inetSocketAddress.getPort(),
                    trafficClass, inetSocketAddress.getAddress());
        } catch (ConnectException e) {
        } finally {
            end(true);
        }
        connectAddress = inetSocketAddress;
        connected = true;
        isBound = true;
        return this;
    }
    @Override
    synchronized public DatagramChannel disconnect() throws IOException {
        if (!isConnected() || !isOpen()) {
            return this;
        }
        connected = false;
        connectAddress = null;
        networkSystem.disconnectDatagram(fd);
        if (null != socket) {
            socket.disconnect();
        }
        return this;
    }
    @Override
    public SocketAddress receive(ByteBuffer target) throws IOException {
        checkWritable(target);
        checkOpen();
        if (!isBound) {
            return null;
        }
        SocketAddress retAddr = null;
        try {
            begin();
            synchronized (readLock) {
                boolean loop = isBlocking();
                if (!target.isDirect()) {
                    retAddr = receiveImpl(target, loop);
                } else {
                    retAddr = receiveDirectImpl(target, loop);
                }
            }
        } catch (InterruptedIOException e) {
            return null;
        } finally {
            end(null != retAddr);
        }
        return retAddr;
    }
    private SocketAddress receiveImpl(ByteBuffer target, boolean loop)
            throws IOException {
        SocketAddress retAddr = null;
        DatagramPacket receivePacket;
        int oldposition = target.position();
        int received = 0;
        if (target.hasArray()) {
            receivePacket = new DatagramPacket(target.array(), target
                    .position()
                    + target.arrayOffset(), target.remaining());
        } else {
            receivePacket = new DatagramPacket(new byte[target.remaining()],
                    target.remaining());
        }
        do {
            if (isConnected()) {
                received = networkSystem.recvConnectedDatagram(fd,
                        receivePacket, receivePacket.getData(), receivePacket
                                .getOffset(), receivePacket.getLength(),
                        isBlocking() ? 0 : DEFAULT_TIMEOUT, false);
            } else {
                received = networkSystem.receiveDatagram(fd, receivePacket,
                        receivePacket.getData(), receivePacket.getOffset(),
                        receivePacket.getLength(), isBlocking() ? 0
                                : DEFAULT_TIMEOUT, false);
            }
            SecurityManager sm = System.getSecurityManager();
            if (!isConnected() && null != sm) {
                try {
                    sm.checkAccept(receivePacket.getAddress().getHostAddress(),
                            receivePacket.getPort());
                } catch (SecurityException e) {
                    receivePacket = null;
                }
            }
            if (null != receivePacket && null != receivePacket.getAddress()) {
                if (received > 0) {
                    if (target.hasArray()) {
                        target.position(oldposition + received);
                    } else {
                        target.put(receivePacket.getData(), 0, received);
                    }
                }
                retAddr = receivePacket.getSocketAddress();
                break;
            }
        } while (loop);
        return retAddr;
    }
    private SocketAddress receiveDirectImpl(ByteBuffer target, boolean loop)
            throws IOException {
        SocketAddress retAddr = null;
        DatagramPacket receivePacket = new DatagramPacket(stubArray, 0);
        int oldposition = target.position();
        int received = 0;
        do {
            int address = AddressUtil.getDirectBufferAddress(target);
            if (isConnected()) {
                received = networkSystem.recvConnectedDatagramDirect(fd,
                        receivePacket, address, target.position(), target
                                .remaining(), isBlocking() ? 0
                                : DEFAULT_TIMEOUT, false);
            } else {
                received = networkSystem.receiveDatagramDirect(fd,
                        receivePacket, address, target.position(), target
                                .remaining(), isBlocking() ? 0
                                : DEFAULT_TIMEOUT, false);
            }
            SecurityManager sm = System.getSecurityManager();
            if (!isConnected() && null != sm) {
                try {
                    sm.checkAccept(receivePacket.getAddress().getHostAddress(),
                            receivePacket.getPort());
                } catch (SecurityException e) {
                    receivePacket = null;
                }
            }
            if (null != receivePacket && null != receivePacket.getAddress()) {
                if (received > 0) {
                    target.position(oldposition + received);
                }
                retAddr = receivePacket.getSocketAddress();
                break;
            }
        } while (loop);
        return retAddr;
    }
    @Override
    public int send(ByteBuffer source, SocketAddress socketAddress)
            throws IOException {
        checkNotNull(source);
        checkOpen();
        InetSocketAddress isa = (InetSocketAddress) socketAddress;
        if (null == isa.getAddress()) {
            throw new IOException();
        }
        if (isConnected()) {
            if (!connectAddress.equals(isa)) {
                throw new IllegalArgumentException();
            }
        } else {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                if (isa.getAddress().isMulticastAddress()) {
                    sm.checkMulticast(isa.getAddress());
                } else {
                    sm.checkConnect(isa.getAddress().getHostAddress(), isa
                            .getPort());
                }
            }
        }
        int sendCount = 0;
        try {
            begin();
            byte[] array = null;
            int length = source.remaining();
            int oldposition = source.position();
            int start = oldposition;
            if (source.isDirect()) {
                synchronized (writeLock) {
                    int data_address = AddressUtil
                            .getDirectBufferAddress(source);
                    sendCount = networkSystem.sendDatagramDirect(fd,
                            data_address, start, length, isa.getPort(), false,
                            trafficClass, isa.getAddress());
                }
            } else {
                if (source.hasArray()) {
                    array = source.array();
                    start += source.arrayOffset();
                } else {
                    array = new byte[length];
                    source.get(array);
                    start = 0;
                }
                synchronized (writeLock) {
                    sendCount = networkSystem.sendDatagram(fd, array, start,
                            length, isa.getPort(), false, trafficClass, isa
                                    .getAddress());
                }
            }
            source.position(oldposition + sendCount);
            return sendCount;
        } finally {
            end(sendCount >= 0);
        }
    }
    @Override
    public int read(ByteBuffer target) throws IOException {
        if (null == target) {
            throw new NullPointerException();
        }
        checkOpenConnected();
        checkWritable(target);
        if (!target.hasRemaining()) {
            return 0;
        }
        int readCount = 0;
        if (target.isDirect() || target.hasArray()) {
            readCount = readImpl(target);
            if (readCount > 0) {
                target.position(target.position() + readCount);
            }
        } else {
            byte[] readArray = new byte[target.remaining()];
            ByteBuffer readBuffer = ByteBuffer.wrap(readArray);
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
        if (length < 0 || offset < 0
                || (long) length + (long) offset > targets.length) {
            throw new IndexOutOfBoundsException();
        }
        checkOpenConnected();
        int totalCount = 0;
        for (int val = offset; val < length; val++) {
            checkWritable(targets[val]);
            totalCount += targets[val].remaining();
        }
        ByteBuffer readBuffer = ByteBuffer.allocate(totalCount);
        int readCount;
        readCount = readImpl(readBuffer);
        int left = readCount;
        int index = offset;
        byte[] readArray = readBuffer.array();
        while (left > 0) {
            int putLength = Math.min(targets[index].remaining(), left);
            targets[index].put(readArray, readCount - left, putLength);
            index++;
            left -= putLength;
        }
        return readCount;
    }
    private int readImpl(ByteBuffer readBuffer) throws IOException {
        synchronized (readLock) {
            int readCount = 0;
            try {
                begin();
                int timeout = isBlocking() ? 0 : DEFAULT_TIMEOUT;
                int start = readBuffer.position();
                int length = readBuffer.remaining();
                if (readBuffer.isDirect()) {
                    int address = AddressUtil.getDirectBufferAddress(readBuffer);
                    if (isConnected()) {
                        readCount = networkSystem.recvConnectedDatagramDirect(
                                fd, null, address, start, length, timeout,
                                false);
                    } else {
                        readCount = networkSystem.receiveDatagramDirect(fd,
                                null, address, start, length, timeout, false);
                    }
                } else {
                    byte[] target = readBuffer.array();
                    start += readBuffer.arrayOffset();
                    if (isConnected()) {
                        readCount = networkSystem.recvConnectedDatagram(fd,
                                null, target, start, length, timeout, false);
                    } else {
                        readCount = networkSystem.receiveDatagram(fd, null,
                                target, start, length, timeout, false);
                    }
                }
                return readCount;
            } catch (InterruptedIOException e) {
                return 0;
            } finally {
                end(readCount > 0);
            }
        }
    }
    @Override
    public int write(ByteBuffer source) throws IOException {
        checkNotNull(source);
        checkOpenConnected();
        if (!source.hasRemaining()) {
            return 0;
        }
        ByteBuffer writeBuffer = null;
        byte[] writeArray = null;
        int oldposition = source.position();
        int result;
        if (source.isDirect() || source.hasArray()) {
            writeBuffer = source;
        } else {
            writeArray = new byte[source.remaining()];
            source.get(writeArray);
            writeBuffer = ByteBuffer.wrap(writeArray);
        }
        result = writeImpl(writeBuffer);
        if (result > 0) {
            source.position(oldposition + result);
        }
        return result;
    }
    @Override
    public long write(ByteBuffer[] sources, int offset, int length)
            throws IOException {
        if (length < 0 || offset < 0
                || (long) length + (long) offset > sources.length) {
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
    private int writeImpl(ByteBuffer buf) throws IOException {
        synchronized (writeLock) {
            int result = 0;
            try {
                begin();
                int length = buf.remaining();
                int start = buf.position();
                if (buf.isDirect()) {
                    int address = AddressUtil.getDirectBufferAddress(buf);
                    result = networkSystem.sendConnectedDatagramDirect(fd,
                            address, start, length, isBound);
                } else {
                    start += buf.arrayOffset();
                    result = networkSystem.sendConnectedDatagram(fd, buf
                            .array(), start, length, isBound);
                }
                return result;
            } finally {
                end(result > 0);
            }
        }
    }
    @Override
    synchronized protected void implCloseSelectableChannel() throws IOException {
        connected = false;
        if (null != socket && !socket.isClosed()) {
            socket.close();
        } else {
            networkSystem.socketClose(fd);
        }
    }
    @Override
    @SuppressWarnings("unused")
    protected void implConfigureBlocking(boolean blockingMode)
            throws IOException {
    }
    private void checkOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }
    private void checkOpenConnected() throws IOException {
        checkOpen();
        if (!isConnected()) {
            throw new NotYetConnectedException();
        }
    }
    private void checkNotNull(ByteBuffer source) {
        if (null == source) {
            throw new NullPointerException();
        }
    }
    private void checkWritable(ByteBuffer target) {
        if (target.isReadOnly()) {
            throw new IllegalArgumentException();
        }
    }
    public FileDescriptor getFD() {
        return fd;
    }
    private int calculateByteBufferArray(ByteBuffer[] sources, int offset,
            int length) {
        int sum = 0;
        for (int val = offset; val < offset + length; val++) {
            sum += sources[val].remaining();
        }
        return sum;
    }
    private static class DatagramSocketAdapter extends DatagramSocket {
        private DatagramChannelImpl channelImpl;
        DatagramSocketAdapter(DatagramSocketImpl socketimpl,
                DatagramChannelImpl channelImpl) {
            super(socketimpl);
            this.channelImpl = channelImpl;
        }
        @Override
        public DatagramChannel getChannel() {
            return channelImpl;
        }
        @Override
        public boolean isBound() {
            return channelImpl.isBound;
        }
        @Override
        public boolean isConnected() {
            return channelImpl.isConnected();
        }
        @Override
        public InetAddress getInetAddress() {
            if (null == channelImpl.connectAddress) {
                return null;
            }
            return channelImpl.connectAddress.getAddress();
        }
        @Override
        public InetAddress getLocalAddress() {
            return channelImpl.getLocalAddress();
        }
        @Override
        public int getPort() {
            if (null == channelImpl.connectAddress) {
                return -1;
            }
            return channelImpl.connectAddress.getPort();
        }
        @Override
        public void bind(SocketAddress localAddr) throws SocketException {
            if (channelImpl.isConnected()) {
                throw new AlreadyConnectedException();
            }
            super.bind(localAddr);
            channelImpl.isBound = true;
        }
        @Override
        public void receive(DatagramPacket packet) throws IOException {
            if (!channelImpl.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            super.receive(packet);
        }
        @Override
        public void send(DatagramPacket packet) throws IOException {
            if (!channelImpl.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            super.send(packet);
        }
        @Override
        public void close() {
            synchronized (channelImpl) {
                if (channelImpl.isOpen()) {
                    try {
                        channelImpl.close();
                    } catch (IOException e) {
                    }
                }
                super.close();
            }
        }
        @Override
        public void disconnect() {
            try {
                channelImpl.disconnect();
            } catch (IOException e) {
            }
            super.disconnect();
        }
    }
}
