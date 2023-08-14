public class PlainSocketImpl extends SocketImpl {
    static final int MULTICAST_IF = 1;
    static final int MULTICAST_TTL = 2;
    static final int TCP_NODELAY = 4;
    static final int FLAG_SHUTDOWN = 8;
    static private InetAddress lastConnectedAddress;
    static private int lastConnectedPort;
    private static Field fdField;
    private static Field localportField;
    private boolean tcpNoDelay = true;
    private int trafficClass;
    protected INetworkSystem netImpl = Platform.getNetworkSystem();
    public int receiveTimeout = 0;
    public boolean streaming = true;
    public boolean shutdownInput;
    Proxy proxy;
    public PlainSocketImpl() {
        super();
        fd = new FileDescriptor();
    }
    public PlainSocketImpl(FileDescriptor fd) {
        super();
        this.fd = fd;
    }
    public PlainSocketImpl(Proxy proxy) {
        this();
        this.proxy = proxy;
    }
    public PlainSocketImpl(FileDescriptor fd, int localport, InetAddress addr, int port) {
        super();
        this.fd = fd;
        this.localport = localport;
        this.address = addr;
        this.port = port;
    }
    @Override
    protected void accept(SocketImpl newImpl) throws IOException {
        if (NetUtil.usingSocks(proxy)) {
            ((PlainSocketImpl) newImpl).socksBind();
            ((PlainSocketImpl) newImpl).socksAccept();
            return;
        }
        try {
            if (newImpl instanceof PlainSocketImpl) {
                PlainSocketImpl newPlainSocketImpl = (PlainSocketImpl) newImpl;
                netImpl.accept(fd, newImpl, newPlainSocketImpl
                        .getFileDescriptor(), receiveTimeout);
                newPlainSocketImpl.setLocalport(getLocalPort());
            } else {
                if (null == fdField) {
                    fdField = getSocketImplField("fd"); 
                }
                FileDescriptor newFd = (FileDescriptor) fdField.get(newImpl);
                netImpl.accept(fd, newImpl, newFd, receiveTimeout);
                if (null == localportField) {
                    localportField = getSocketImplField("localport"); 
                }
                localportField.setInt(newImpl, getLocalPort());
            }
        } catch (InterruptedIOException e) {
            throw new SocketTimeoutException(e.getMessage());
        } catch (IllegalAccessException e) {
        }
    }
    private Field getSocketImplField(final String fieldName) {
        return AccessController.doPrivileged(new PrivilegedAction<Field>() {
            public Field run() {
                Field field = null;
                try {
                    field = SocketImpl.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
                return field;
            }
        });
    }
    @Override
    protected synchronized int available() throws IOException {
        if (shutdownInput == true) {
            return 0;
        }
        return netImpl.availableStream(fd);
    }
    @Override
    protected void bind(InetAddress anAddr, int aPort) throws IOException {
        netImpl.bind(fd, anAddr, aPort);
        address = anAddr;
        if (0 != aPort) {
            localport = aPort;
        } else {
            localport = netImpl.getSocketLocalPort(fd);
        }
    }
    @Override
    protected void close() throws IOException {
        synchronized (fd) {
            if (fd.valid()) {
                if ((netImpl.getSocketFlags() & FLAG_SHUTDOWN) != 0) {
                    try {
                        shutdownOutput();
                    } catch (Exception e) {
                    }
                }
                netImpl.socketClose(fd);
                fd = new FileDescriptor();
            }
        }
    }
    @Override
    protected void connect(String aHost, int aPort) throws IOException {
        connect(netImpl.getHostByName(aHost), aPort);
    }
    @Override
    protected void connect(InetAddress anAddr, int aPort) throws IOException {
        connect(anAddr, aPort, 0);
    }
    private void connect(InetAddress anAddr, int aPort, int timeout)
            throws IOException {
        InetAddress normalAddr = anAddr.isAnyLocalAddress() ? InetAddress.getLocalHost() : anAddr;
        try {
            if (streaming) {
                if (NetUtil.usingSocks(proxy)) {
                    socksConnect(anAddr, aPort, 0);
                } else {
                    if (timeout == 0) {
                        netImpl.connect(fd, trafficClass, normalAddr, aPort);
                    } else {
                        netImpl.connectStreamWithTimeoutSocket(fd, aPort,
                                timeout, trafficClass, normalAddr);
                    }
                }
            } else {
            	netImpl.connectDatagram(fd, aPort, trafficClass, normalAddr);
            }
        } catch (ConnectException e) {
            throw new ConnectException(anAddr + ":" + aPort + " - "
                    + e.getMessage());
        }
        super.address = normalAddr;
        super.port = aPort;
    }
    @Override
    protected void create(boolean streaming) throws IOException {
        this.streaming = streaming;
        if (streaming) {
            netImpl.createStreamSocket(fd, NetUtil.preferIPv4Stack());
        } else {
            netImpl.createDatagramSocket(fd, NetUtil.preferIPv4Stack());
        }
    }
    @Override
    protected void finalize() throws IOException {
        close();
    }
    @Override
    protected synchronized InputStream getInputStream() throws IOException {
        if (!fd.valid()) {
            throw new SocketException(Msg.getString("K003d"));
        }
        return new SocketInputStream(this);
    }
    @Override
    public Object getOption(int optID) throws SocketException {
        if (optID == SocketOptions.SO_TIMEOUT) {
            return Integer.valueOf(receiveTimeout);
        } else if (optID == SocketOptions.IP_TOS) {
            return Integer.valueOf(trafficClass);
        } else {
            Object result = netImpl.getSocketOption(fd, optID);
            if (optID == SocketOptions.TCP_NODELAY
                    && (netImpl.getSocketFlags() & TCP_NODELAY) != 0) {
                return Boolean.valueOf(tcpNoDelay);
            }
            return result;
        }
    }
    @Override
    protected synchronized OutputStream getOutputStream() throws IOException {
        if (!fd.valid()) {
            throw new SocketException(Msg.getString("K003d")); 
        }
        return new SocketOutputStream(this);
    }
    @Override
    protected void listen(int backlog) throws IOException {
        if (NetUtil.usingSocks(proxy)) {
            return;
        }
        netImpl.listenStreamSocket(fd, backlog);
    }
    @Override
    public void setOption(int optID, Object val) throws SocketException {
        if (optID == SocketOptions.SO_TIMEOUT) {
            receiveTimeout = ((Integer) val).intValue();
        } else {
            try {
                netImpl.setSocketOption(fd, optID, val);
                if (optID == SocketOptions.TCP_NODELAY
                        && (netImpl.getSocketFlags() & TCP_NODELAY) != 0) {
                    tcpNoDelay = ((Boolean) val).booleanValue();
                }
            } catch (SocketException e) {
                if (optID != SocketOptions.IP_TOS) {
                    throw e;
                }
            }
            if (optID == SocketOptions.IP_TOS) {
                trafficClass = ((Integer) val).intValue();
            }
        }
    }
    private int socksGetServerPort() {
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        return addr.getPort();
    }
    private InetAddress socksGetServerAddress() throws UnknownHostException {
        String proxyName;
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        proxyName = addr.getHostName();
        if (null == proxyName) {
            proxyName = addr.getAddress().getHostAddress();
        }
        return netImpl.getHostByName(proxyName);
    }
    private void socksConnect(InetAddress applicationServerAddress,
            int applicationServerPort, int timeout) throws IOException {
        try {
            if (timeout == 0) {
                netImpl.connect(fd, trafficClass, socksGetServerAddress(),
                        socksGetServerPort());
            } else {
                netImpl.connectStreamWithTimeoutSocket(fd,
                        socksGetServerPort(), timeout, trafficClass,
                        socksGetServerAddress());
            }
        } catch (Exception e) {
            throw new SocketException(Msg.getString("K003e", e)); 
        }
        socksRequestConnection(applicationServerAddress, applicationServerPort);
        lastConnectedAddress = applicationServerAddress;
        lastConnectedPort = applicationServerPort;
    }
    private void socksRequestConnection(InetAddress applicationServerAddress,
            int applicationServerPort) throws IOException {
        socksSendRequest(Socks4Message.COMMAND_CONNECT,
                applicationServerAddress, applicationServerPort);
        Socks4Message reply = socksReadReply();
        if (reply.getCommandOrResult() != Socks4Message.RETURN_SUCCESS) {
            throw new IOException(reply.getErrorString(reply
                    .getCommandOrResult()));
        }
    }
    public void socksAccept() throws IOException {
        Socks4Message reply = socksReadReply();
        if (reply.getCommandOrResult() != Socks4Message.RETURN_SUCCESS) {
            throw new IOException(reply.getErrorString(reply
                    .getCommandOrResult()));
        }
    }
    @Override
    protected void shutdownInput() throws IOException {
        shutdownInput = true;
        netImpl.shutdownInput(fd);
    }
    @Override
    protected void shutdownOutput() throws IOException {
        netImpl.shutdownOutput(fd);
    }
    private void socksBind() throws IOException {
        try {
            netImpl.connect(fd, trafficClass, socksGetServerAddress(),
                    socksGetServerPort());
        } catch (Exception e) {
            throw new IOException(Msg.getString("K003f", e)); 
        }
        if (lastConnectedAddress == null) {
            throw new SocketException(Msg.getString("K0040")); 
        }
        socksSendRequest(Socks4Message.COMMAND_BIND, lastConnectedAddress,
                lastConnectedPort);
        Socks4Message reply = socksReadReply();
        if (reply.getCommandOrResult() != Socks4Message.RETURN_SUCCESS) {
            throw new IOException(reply.getErrorString(reply
                    .getCommandOrResult()));
        }
        if (reply.getIP() == 0) {
            address = socksGetServerAddress();
        } else {
            byte[] replyBytes = new byte[4];
            NetUtil.intToBytes(reply.getIP(), replyBytes, 0);
            address = InetAddress.getByAddress(replyBytes);
        }
        localport = reply.getPort();
    }
    private void socksSendRequest(int command, InetAddress address, int port)
            throws IOException {
        Socks4Message request = new Socks4Message();
        request.setCommandOrResult(command);
        request.setPort(port);
        request.setIP(address.getAddress());
        request.setUserId("default"); 
        getOutputStream().write(request.getBytes(), 0, request.getLength());
    }
    private Socks4Message socksReadReply() throws IOException {
        Socks4Message reply = new Socks4Message();
        int bytesRead = 0;
        while (bytesRead < Socks4Message.REPLY_LENGTH) {
            int count = getInputStream().read(reply.getBytes(), bytesRead,
                    Socks4Message.REPLY_LENGTH - bytesRead);
            if (-1 == count) {
                break;
            }
            bytesRead += count;
        }
        if (Socks4Message.REPLY_LENGTH != bytesRead) {
            throw new SocketException(Msg.getString("KA011")); 
        }
        return reply;
    }
    @Override
    protected void connect(SocketAddress remoteAddr, int timeout)
            throws IOException {
        InetSocketAddress inetAddr = (InetSocketAddress) remoteAddr;
        connect(inetAddr.getAddress(), inetAddr.getPort(), timeout);
    }
    @Override
    protected boolean supportsUrgentData() {
        return !streaming || netImpl.supportsUrgentData(fd);
    }
    @Override
    protected void sendUrgentData(int value) throws IOException {
        netImpl.sendUrgentData(fd, (byte) value);
    }
    FileDescriptor getFD() {
        return fd;
    }
    private void setLocalport(int localport) {
        this.localport = localport;
    }
    int read(byte[] buffer, int offset, int count) throws IOException {
        if (shutdownInput) {
            return -1;
        }
        int read = netImpl.read(fd, buffer, offset, count, receiveTimeout);
        if (read == 0) {
            throw new SocketTimeoutException();
        }
        if (read == -1) {
            shutdownInput = true;
        }
        return read;
    }
    int write(byte[] buffer, int offset, int count) throws IOException {
        if (!streaming) {
            return netImpl.sendDatagram2(fd, buffer, offset, count, port,
                    address);
        }
        return netImpl.write(fd, buffer, offset, count);
    }
}
