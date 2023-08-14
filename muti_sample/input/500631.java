public class ServerSocket {
    SocketImpl impl;
    static SocketImplFactory factory;
    private volatile boolean isCreated;
    private boolean isBound;
    private boolean isClosed;
    public ServerSocket() throws IOException {
        impl = factory != null ? factory.createSocketImpl()
                : new PlainServerSocketImpl();
    }
    protected ServerSocket(SocketImpl impl) {
        this.impl = impl;
    }
    public ServerSocket(int aport) throws IOException {
        this(aport, defaultBacklog(), Inet4Address.ANY);
    }
    public ServerSocket(int aport, int backlog) throws IOException {
        this(aport, backlog, Inet4Address.ANY);
    }
    public ServerSocket(int aport, int backlog, InetAddress localAddr)
            throws IOException {
        super();
        checkListen(aport);
        impl = factory != null ? factory.createSocketImpl()
                : new PlainServerSocketImpl();
        InetAddress addr = localAddr == null ? Inet4Address.ANY : localAddr;
        synchronized (this) {
            impl.create(true);
            isCreated = true;
            try {
                impl.bind(addr, aport);
                isBound = true;
                impl.listen(backlog > 0 ? backlog : defaultBacklog());
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }
    public Socket accept() throws IOException {
        checkClosedAndCreate(false);
        if (!isBound()) {
            throw new SocketException(Msg.getString("K031f")); 
        }
        Socket aSocket = new Socket();
        try {
            implAccept(aSocket);
        } catch (SecurityException e) {
            aSocket.close();
            throw e;
        } catch (IOException e) {
            aSocket.close();
            throw e;
        }
        return aSocket;
    }
    void checkListen(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0325", aPort)); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(aPort);
        }
    }
    public void close() throws IOException {
        isClosed = true;
        impl.close();
    }
    static int defaultBacklog() {
        return 50;
    }
    public InetAddress getInetAddress() {
        if (!isBound()) {
            return null;
        }
        return impl.getInetAddress();
    }
    public int getLocalPort() {
        if (!isBound()) {
            return -1;
        }
        return impl.getLocalPort();
    }
    public synchronized int getSoTimeout() throws IOException {
        if (!isCreated) {
            synchronized (this) {
                if (!isCreated) {
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
        }
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }
    protected final void implAccept(Socket aSocket) throws IOException {
        synchronized (this) {
            impl.accept(aSocket.impl);
            aSocket.accepted();
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccept(aSocket.getInetAddress().getHostAddress(),
                    aSocket.getPort());
        }
    }
    public static synchronized void setSocketFactory(SocketImplFactory aFactory)
            throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        if (factory != null) {
            throw new SocketException(Msg.getString("K0042")); 
        }
        factory = aFactory;
    }
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        checkClosedAndCreate(true);
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); 
        }
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(64);
        result.append("ServerSocket["); 
        if (!isBound()) {
            return result.append("unbound]").toString(); 
        }
        return result.append("addr=") 
                .append(getInetAddress().getHostName()).append("/") 
                .append(getInetAddress().getHostAddress()).append(
                        ",port=0,localport=") 
                .append(getLocalPort()).append("]") 
                .toString();
    }
    public void bind(SocketAddress localAddr) throws IOException {
        bind(localAddr, defaultBacklog());
    }
    public void bind(SocketAddress localAddr, int backlog) throws IOException {
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
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(port);
        }
        synchronized (this) {
            try {
                impl.bind(addr, port);
                isBound = true;
                impl.listen(backlog > 0 ? backlog : defaultBacklog());
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }
    public boolean isBound() {
        return isBound;
    }
    public boolean isClosed() {
        return isClosed;
    }
    private void checkClosedAndCreate(boolean create) throws SocketException {
        if (isClosed()) {
            throw new SocketException(Msg.getString("K003d")); 
        }
        if (!create || isCreated) {
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
    public void setReceiveBufferSize(int size) throws SocketException {
        checkClosedAndCreate(true);
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); 
        }
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }
    public int getReceiveBufferSize() throws SocketException {
        checkClosedAndCreate(true);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }
    public ServerSocketChannel getChannel() {
        return null;
    }
    public void setPerformancePreferences(int connectionTime, int latency,
            int bandwidth) {
    }
}
