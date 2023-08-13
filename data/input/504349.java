public class Socket {
    SocketImpl impl;
    static SocketImplFactory factory;
    private volatile boolean isCreated = false;
    private boolean isBound = false;
    private boolean isConnected = false;
    private boolean isClosed = false;
    private boolean isInputShutdown = false;
    private boolean isOutputShutdown = false;
    private static class ConnectLock {
    }
    private Object connectLock = new ConnectLock();
    private Proxy proxy;
    static final int MULTICAST_IF = 1;
    static final int MULTICAST_TTL = 2;
    static final int TCP_NODELAY = 4;
    static final int FLAG_SHUTDOWN = 8;
    static private Logger logger;
    static private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(Socket.class.getName());
        }
        return logger;
    }
    public Socket() {
        impl = factory != null ? factory.createSocketImpl()
                : new PlainSocketImpl();
    }
    public Socket(Proxy proxy) {
        if (null == proxy || Proxy.Type.HTTP == proxy.type()) {
            throw new IllegalArgumentException(Msg.getString("KA023")); 
        }
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        if (null != address) {
            InetAddress addr = address.getAddress();
            String host;
            if (null != addr) {
                host = addr.getHostAddress();
            } else {
                host = address.getHostName();
            }
            int port = address.getPort();
            checkConnectPermission(host, port);
        }
        impl = factory != null ? factory.createSocketImpl()
                : new PlainSocketImpl(proxy);
        this.proxy = proxy;
    }
    private void tryAllAddresses(String dstName, int dstPort, InetAddress
            localAddress, int localPort, boolean streaming) throws IOException {
        InetAddress[] dstAddresses = InetAddress.getAllByName(dstName);
        InetAddress dstAddress;
        for (int i = 0; i < dstAddresses.length - 1; i++) {
            dstAddress = dstAddresses[i];
            try {
                checkDestination(dstAddress, dstPort);
                startupSocket(dstAddress, dstPort, localAddress, localPort,
                        streaming);
                return;
            } catch(SecurityException e1) {
                getLogger().log(Level.INFO, dstAddress + "(" + dstPort + "): " +
                        e1.getClass().getName() + ": " + e1.getMessage());
            } catch(IOException e2) {
                getLogger().log(Level.INFO, dstAddress + "(" + dstPort + "): " +
                        e2.getClass().getName() + ": " + e2.getMessage());
            }
        }
        dstAddress = dstAddresses[dstAddresses.length - 1];
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, localAddress, localPort, streaming);
    }
    public Socket(String dstName, int dstPort) throws UnknownHostException,
            IOException {
        this(dstName, dstPort, null, 0);
    }
    public Socket(String dstName, int dstPort, InetAddress localAddress,
            int localPort) throws IOException {
        this();
        tryAllAddresses(dstName, dstPort, localAddress, localPort, true);
    }
    @Deprecated
    public Socket(String hostName, int port, boolean streaming)
            throws IOException {
        this();
        tryAllAddresses(hostName, port, null, 0, streaming);
    }
    public Socket(InetAddress dstAddress, int dstPort) throws IOException {
        this();
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, null, 0, true);
    }
    public Socket(InetAddress dstAddress, int dstPort,
            InetAddress localAddress, int localPort) throws IOException {
        this();
        checkDestination(dstAddress, dstPort);
        startupSocket(dstAddress, dstPort, localAddress, localPort, true);
    }
    @Deprecated
    public Socket(InetAddress addr, int port, boolean streaming)
            throws IOException {
        this();
        checkDestination(addr, port);
        startupSocket(addr, port, null, 0, streaming);
    }
    protected Socket(SocketImpl anImpl) throws SocketException {
        impl = anImpl;
    }
    void checkDestination(InetAddress destAddr, int dstPort) {
        if (dstPort < 0 || dstPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0032")); 
        }
        checkConnectPermission(destAddr.getHostAddress(), dstPort);
    }
    private void checkConnectPermission(String hostname, int dstPort) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkConnect(hostname, dstPort);
        }
    }
    public synchronized void close() throws IOException {
        isClosed = true;
        impl.close();
    }
    public InetAddress getInetAddress() {
        if (!isConnected()) {
            return null;
        }
        return impl.getInetAddress();
    }
    public InputStream getInputStream() throws IOException {
        checkClosedAndCreate(false);
        if (isInputShutdown()) {
            throw new SocketException(Msg.getString("K0321")); 
        }
        return impl.getInputStream();
    }
    public boolean getKeepAlive() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_KEEPALIVE))
                .booleanValue();
    }
    public InetAddress getLocalAddress() {
        if (!isBound()) {
            return Inet4Address.ANY;
        }
        return Platform.getNetworkSystem().getSocketLocalAddress(impl.fd);
    }
    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        return impl.getLocalPort();
    }
    public OutputStream getOutputStream() throws IOException {
        checkClosedAndCreate(false);
        if (isOutputShutdown()) {
            throw new SocketException(Msg.getString("KA00f")); 
        }
        return impl.getOutputStream();
    }
    public int getPort() {
        if (!isConnected()) {
            return 0;
        }
        return impl.getPort();
    }
    public int getSoLinger() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_LINGER)).intValue();
    }
    public synchronized int getReceiveBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }
    public synchronized int getSendBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_SNDBUF)).intValue();
    }
    public synchronized int getSoTimeout() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }
    public boolean getTcpNoDelay() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.TCP_NODELAY))
                .booleanValue();
    }
    public void setKeepAlive(boolean value) throws SocketException {
        if (impl != null) {
            checkClosedAndCreate(true);
            impl.setOption(SocketOptions.SO_KEEPALIVE, value ? Boolean.TRUE
                    : Boolean.FALSE);
        }
    }
    public static synchronized void setSocketImplFactory(SocketImplFactory fac)
            throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        if (factory != null) {
            throw new SocketException(Msg.getString("K0044")); 
        }
        factory = fac;
    }
    public synchronized void setSendBufferSize(int size) throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); 
        }
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size));
    }
    public synchronized void setReceiveBufferSize(int size)
            throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); 
        }
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }
    public void setSoLinger(boolean on, int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (on && timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0045")); 
        }
        if (on) {
            if (timeout > 65535) {
                timeout = 65535;
            }
            impl.setOption(SocketOptions.SO_LINGER, Integer.valueOf(timeout));
        } else {
            impl.setOption(SocketOptions.SO_LINGER, Boolean.FALSE);
        }
    }
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); 
        }
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }
    public void setTcpNoDelay(boolean on) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.TCP_NODELAY, Boolean.valueOf(on));
    }
    void startupSocket(InetAddress dstAddress, int dstPort,
            InetAddress localAddress, int localPort, boolean streaming)
            throws IOException {
        if (localPort < 0 || localPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0046")); 
        }
        InetAddress addr = localAddress == null ? Inet4Address.ANY
                : localAddress;
        synchronized (this) {
            impl.create(streaming);
            isCreated = true;
            try {
                if (!streaming || !NetUtil.usingSocks(proxy)) {
                    impl.bind(addr, localPort);
                }
                isBound = true;
                impl.connect(dstAddress, dstPort);
                isConnected = true;
            } catch (IOException e) {
                impl.close();
                throw e;
            }
        }
    }
    @Override
    public String toString() {
        if (!isConnected()) {
            return "Socket[unconnected]"; 
        }
        return impl.toString();
    }
    public void shutdownInput() throws IOException {
        if (isInputShutdown()) {
            throw new SocketException(Msg.getString("K0321")); 
        }
        checkClosedAndCreate(false);
        impl.shutdownInput();
        isInputShutdown = true;
    }
    public void shutdownOutput() throws IOException {
        if (isOutputShutdown()) {
            throw new SocketException(Msg.getString("KA00f")); 
        }
        checkClosedAndCreate(false);
        impl.shutdownOutput();
        isOutputShutdown = true;
    }
    private void checkClosedAndCreate(boolean create) throws SocketException {
        if (isClosed()) {
            throw new SocketException(Msg.getString("K003d")); 
        }
        if (!create) {
            if (!isConnected()) {
                throw new SocketException(Msg.getString("K0320")); 
            }
            return;
        }
        if (isCreated) {
            return;
        }
        synchronized (this) {
            if (isCreated) {
                return;
            }
            try {
                impl.create(true);
            } catch (SocketException e) {
                throw e;
            } catch (IOException e) {
                throw new SocketException(e.toString());
            }
            isCreated = true;
        }
    }
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }
    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getPort());
    }
    public boolean isBound() {
        return isBound;
    }
    public boolean isConnected() {
        return isConnected;
    }
    public boolean isClosed() {
        return isClosed;
    }
    public void bind(SocketAddress localAddr) throws IOException {
        checkClosedAndCreate(true);
        if (isBound()) {
            throw new BindException(Msg.getString("K0315")); 
        }
        int port = 0;
        InetAddress addr = Inet4Address.ANY;
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(Msg.getString(
                        "K0316", localAddr.getClass())); 
            }
            InetSocketAddress inetAddr = (InetSocketAddress) localAddr;
            if ((addr = inetAddr.getAddress()) == null) {
                throw new SocketException(Msg.getString(
                        "K0317", inetAddr.getHostName())); 
            }
            port = inetAddr.getPort();
        }
        synchronized (this) {
            try {
                impl.bind(addr, port);
                isBound = true;
            } catch (IOException e) {
                impl.close();
                throw e;
            }
        }
    }
    public void connect(SocketAddress remoteAddr) throws IOException {
        connect(remoteAddr, 0);
    }
    public void connect(SocketAddress remoteAddr, int timeout)
            throws IOException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); 
        }
        if (isConnected()) {
            throw new SocketException(Msg.getString("K0079")); 
        }
        if (remoteAddr == null) {
            throw new IllegalArgumentException(Msg.getString("K0318")); 
        }
        if (!(remoteAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", remoteAddr.getClass())); 
        }
        InetSocketAddress inetAddr = (InetSocketAddress) remoteAddr;
        InetAddress addr;
        if ((addr = inetAddr.getAddress()) == null) {
            throw new UnknownHostException(Msg.getString("K0317", remoteAddr));
        }
        int port = inetAddr.getPort();
        checkDestination(addr, port);
        synchronized (connectLock) {
            try {
                if (!isBound()) {
                    if (!NetUtil.usingSocks(proxy)) {
                        impl.bind(Inet4Address.ANY, 0);
                    }
                    isBound = true;
                }
                impl.connect(remoteAddr, timeout);
                isConnected = true;
            } catch (IOException e) {
                impl.close();
                throw e;
            }
        }
    }
    public boolean isInputShutdown() {
        return isInputShutdown;
    }
    public boolean isOutputShutdown() {
        return isOutputShutdown;
    }
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.SO_REUSEADDR, reuse ? Boolean.TRUE
                : Boolean.FALSE);
    }
    public boolean getReuseAddress() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR))
                .booleanValue();
    }
    public void setOOBInline(boolean oobinline) throws SocketException {
        checkClosedAndCreate(true);
        impl.setOption(SocketOptions.SO_OOBINLINE, oobinline ? Boolean.TRUE
                : Boolean.FALSE);
    }
    public boolean getOOBInline() throws SocketException {
        checkClosedAndCreate(true);
        return ((Boolean) impl.getOption(SocketOptions.SO_OOBINLINE))
                .booleanValue();
    }
    public void setTrafficClass(int value) throws SocketException {
        checkClosedAndCreate(true);
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException();
        }
        impl.setOption(SocketOptions.IP_TOS, Integer.valueOf(value));
    }
    public int getTrafficClass() throws SocketException {
        checkClosedAndCreate(true);
        return ((Number) impl.getOption(SocketOptions.IP_TOS)).intValue();
    }
    public void sendUrgentData(int value) throws IOException {
        if (!impl.supportsUrgentData()) {
            throw new SocketException(Msg.getString("K0333")); 
        }
        impl.sendUrgentData(value);
    }
    void accepted() {
        isCreated = isBound = isConnected = true;
    }
    static boolean preferIPv4Stack() {
        String result = AccessController.doPrivileged(new PriviAction<String>(
                "java.net.preferIPv4Stack")); 
        return "true".equals(result); 
    }
    public SocketChannel getChannel() {
        return null;
    }
    public void setPerformancePreferences(int connectionTime, int latency,
            int bandwidth) {
    }
}
