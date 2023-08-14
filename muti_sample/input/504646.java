public class DatagramSocket {
    DatagramSocketImpl impl;
    InetAddress address;
    int port = -1;
    static DatagramSocketImplFactory factory;
    boolean isBound = false;
    private boolean isConnected = false;
    private boolean isClosed = false;
    private static class Lock {
    }
    private Object lock = new Lock();
    public DatagramSocket() throws SocketException {
        this(0);
    }
    public DatagramSocket(int aPort) throws SocketException {
        super();
        checkListen(aPort);
        createSocket(aPort, Inet4Address.ANY);
    }
    public DatagramSocket(int aPort, InetAddress addr) throws SocketException {
        super();
        checkListen(aPort);
        createSocket(aPort, null == addr ? Inet4Address.ANY : addr);
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
    public void close() {
        isClosed = true;
        impl.close();
    }
    public void connect(InetAddress anAddress, int aPort) {
        if (anAddress == null || aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0032")); 
        }
        synchronized (lock) {
            if (isClosed()) {
                return;
            }
            try {
                checkClosedAndBind(true);
            } catch (SocketException e) {
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (anAddress.isMulticastAddress()) {
                    security.checkMulticast(anAddress);
                } else {
                    security.checkConnect(anAddress.getHostName(), aPort);
                }
            }
            try {
                impl.connect(anAddress, aPort);
            } catch (SocketException e) {
            }
            address = anAddress;
            port = aPort;
            isConnected = true;
        }
    }
    public void disconnect() {
        if (isClosed() || !isConnected()) {
            return;
        }
        impl.disconnect();
        address = null;
        port = -1;
        isConnected = false;
    }
    synchronized void createSocket(int aPort, InetAddress addr)
            throws SocketException {
        impl = factory != null ? factory.createDatagramSocketImpl()
                : new PlainDatagramSocketImpl();
        impl.create();
        try {
            impl.bind(aPort, addr);
            isBound = true;
        } catch (SocketException e) {
            close();
            throw e;
        }
    }
    public InetAddress getInetAddress() {
        return address;
    }
    public InetAddress getLocalAddress() {
        if (isClosed()) {
            return null;
        }
        if (!isBound()) {
            return Inet4Address.ANY;
        }
        InetAddress anAddr = impl.getLocalAddress();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(anAddr.getHostName(), -1);
            }
        } catch (SecurityException e) {
            return Inet4Address.ANY;
        }
        return anAddr;
    }
    public int getLocalPort() {
        if (isClosed()) {
            return -1;
        }
        if (!isBound()) {
            return 0;
        }
        return impl.getLocalPort();
    }
    public int getPort() {
        return port;
    }
    boolean isMulticastSocket() {
        return false;
    }
    public synchronized int getReceiveBufferSize() throws SocketException {
        checkClosedAndBind(false);
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }
    public synchronized int getSendBufferSize() throws SocketException {
        checkClosedAndBind(false);
        return ((Integer) impl.getOption(SocketOptions.SO_SNDBUF)).intValue();
    }
    public synchronized int getSoTimeout() throws SocketException {
        checkClosedAndBind(false);
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }
    public synchronized void receive(DatagramPacket pack) throws IOException {
        checkClosedAndBind(true);
        InetAddress senderAddr;
        int senderPort;
        DatagramPacket tempPack = new DatagramPacket(new byte[1], 1);
        boolean copy = false;
        SecurityManager security = System.getSecurityManager();
        if (address != null || security != null) {
            if (pack == null) {
                throw new NullPointerException();
            }
            while (true) {
                copy = false;
                try {
                    senderPort = impl.peekData(tempPack);
                    senderAddr = tempPack.getAddress();
                } catch (SocketException e) {
                    if (e.getMessage().equals(
                            "The socket does not support the operation")) { 
                        tempPack = new DatagramPacket(new byte[pack.getCapacity()],
                                pack.getCapacity());
                        impl.receive(tempPack);
                        senderAddr = tempPack.getAddress();
                        senderPort = tempPack.getPort();
                        copy = true;
                    } else {
                        throw e;
                    }
                }
                if (address == null) {
                    try {
                        security.checkAccept(senderAddr.getHostName(),
                                senderPort);
                        break;
                    } catch (SecurityException e) {
                        if (!copy) {
                            impl.receive(tempPack);
                        }
                    }
                } else if (port == senderPort && address.equals(senderAddr)) {
                    break;
                } else if (!copy) {
                    impl.receive(tempPack);
                }
            }
        }
        if (copy) {
            System.arraycopy(tempPack.getData(), 0, pack.getData(), pack
                    .getOffset(), tempPack.getLength());
            pack.setLengthOnly(tempPack.getLength());
            pack.setAddress(tempPack.getAddress());
            pack.setPort(tempPack.getPort());
        } else {
            pack.setLength(pack.getCapacity());
            impl.receive(pack);
        }
    }
    public void send(DatagramPacket pack) throws IOException {
        checkClosedAndBind(true);
        InetAddress packAddr = pack.getAddress();
        if (address != null) { 
            if (packAddr != null) {
                if (!address.equals(packAddr) || port != pack.getPort()) {
                    throw new IllegalArgumentException(Msg.getString("K0034")); 
                }
            } else {
                pack.setAddress(address);
                pack.setPort(port);
            }
        } else {
            if (packAddr == null) {
                if (pack.getPort() == -1) {
                    throw new NullPointerException(Msg.getString("KA019")); 
                }
                return;
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (packAddr.isMulticastAddress()) {
                    security.checkMulticast(packAddr);
                } else {
                    security.checkConnect(packAddr.getHostName(), pack
                            .getPort());
                }
            }
        }
        impl.send(pack);
    }
    public synchronized void setSendBufferSize(int size) throws SocketException {
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); 
        }
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size));
    }
    public synchronized void setReceiveBufferSize(int size)
            throws SocketException {
        if (size < 1) {
            throw new IllegalArgumentException(Msg.getString("K0035")); 
        }
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0) {
            throw new IllegalArgumentException(Msg.getString("K0036")); 
        }
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout));
    }
    public static synchronized void setDatagramSocketImplFactory(
            DatagramSocketImplFactory fac) throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        if (factory != null) {
            throw new SocketException(Msg.getString("K0044")); 
        }
        factory = fac;
    }
    protected DatagramSocket(DatagramSocketImpl socketImpl) {
        if (socketImpl == null) {
            throw new NullPointerException();
        }
        impl = socketImpl;
    }
    public DatagramSocket(SocketAddress localAddr) throws SocketException {
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(Msg.getString(
                        "K0316", localAddr.getClass())); 
            }
            checkListen(((InetSocketAddress) localAddr).getPort());
        }
        impl = factory != null ? factory.createDatagramSocketImpl()
                : new PlainDatagramSocketImpl();
        impl.create();
        if (localAddr != null) {
            try {
                bind(localAddr);
            } catch (SocketException e) {
                close();
                throw e;
            }
        }
        setBroadcast(true);
    }
    void checkClosedAndBind(boolean bind) throws SocketException {
        if (isClosed()) {
            throw new SocketException(Msg.getString("K003d")); 
        }
        if (bind && !isBound()) {
            checkListen(0);
            impl.bind(0, Inet4Address.ANY);
            isBound = true;
        }
    }
    public void bind(SocketAddress localAddr) throws SocketException {
        checkClosedAndBind(false);
        int localPort = 0;
        InetAddress addr = Inet4Address.ANY;
        if (localAddr != null) {
            if (!(localAddr instanceof InetSocketAddress)) {
                throw new IllegalArgumentException(Msg.getString(
                        "K0316", localAddr.getClass())); 
            }
            InetSocketAddress inetAddr = (InetSocketAddress) localAddr;
            addr = inetAddr.getAddress();
            if (addr == null) {
                throw new SocketException(Msg.getString(
                        "K0317", inetAddr.getHostName())); 
            }
            localPort = inetAddr.getPort();
            checkListen(localPort);
        }
        impl.bind(localPort, addr);
        isBound = true;
    }
    public void connect(SocketAddress remoteAddr) throws SocketException {
        if (remoteAddr == null) {
            throw new IllegalArgumentException(Msg.getString("K0318")); 
        }
        if (!(remoteAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", remoteAddr.getClass())); 
        }
        InetSocketAddress inetAddr = (InetSocketAddress) remoteAddr;
        if (inetAddr.getAddress() == null) {
            throw new SocketException(Msg.getString(
                    "K0317", inetAddr.getHostName())); 
        }
        synchronized (lock) {
            checkClosedAndBind(true);
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (inetAddr.getAddress().isMulticastAddress()) {
                    security.checkMulticast(inetAddr.getAddress());
                } else {
                    security.checkConnect(inetAddr.getAddress().getHostName(),
                            inetAddr.getPort());
                }
            }
            try {
                impl.connect(inetAddr.getAddress(), inetAddr.getPort());
            } catch (Exception e) {
            }
            address = inetAddr.getAddress();
            port = inetAddr.getPort();
            isConnected = true;
        }
    }
    public boolean isBound() {
        return isBound;
    }
    public boolean isConnected() {
        return isConnected;
    }
    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected()) {
            return null;
        }
        return new InetSocketAddress(getInetAddress(), getPort());
    }
    public SocketAddress getLocalSocketAddress() {
        if (!isBound()) {
            return null;
        }
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }
    public void setReuseAddress(boolean reuse) throws SocketException {
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_REUSEADDR, reuse ? Boolean.TRUE
                : Boolean.FALSE);
    }
    public boolean getReuseAddress() throws SocketException {
        checkClosedAndBind(false);
        return ((Boolean) impl.getOption(SocketOptions.SO_REUSEADDR))
                .booleanValue();
    }
    public void setBroadcast(boolean broadcast) throws SocketException {
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.SO_BROADCAST, broadcast ? Boolean.TRUE
                : Boolean.FALSE);
    }
    public boolean getBroadcast() throws SocketException {
        checkClosedAndBind(false);
        return ((Boolean) impl.getOption(SocketOptions.SO_BROADCAST))
                .booleanValue();
    }
    public void setTrafficClass(int value) throws SocketException {
        checkClosedAndBind(false);
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException();
        }
        impl.setOption(SocketOptions.IP_TOS, Integer.valueOf(value));
    }
    public int getTrafficClass() throws SocketException {
        checkClosedAndBind(false);
        return ((Number) impl.getOption(SocketOptions.IP_TOS)).intValue();
    }
    public boolean isClosed() {
        return isClosed;
    }
    public DatagramChannel getChannel() {
        return null;
    }
}
