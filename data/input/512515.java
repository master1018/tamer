public class InetSocketAddress extends SocketAddress {
    private static final long serialVersionUID = 5076001401234631237L;
    private String hostname;
    private InetAddress addr;
    private int port;
    public InetSocketAddress(int port) {
        this((InetAddress) null, port);
    }
    public InetSocketAddress(InetAddress address, int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException();
        }
        if (address == null) {
            addr = Inet4Address.ANY;
        } else {
            addr = address;
        }
        hostname = addr.getHostName();
        this.port = port;
    }
    public InetSocketAddress(String host, int port) {
        this(host, port, true);
    }
    InetSocketAddress(String host, int port, boolean needResolved) {
        if (host == null || port < 0 || port > 65535) {
            throw new IllegalArgumentException();
        }
        hostname = host;
        this.port = port;
        if (needResolved) {
            SecurityManager smgr = System.getSecurityManager();
            if(smgr != null) {
                smgr.checkConnect(host, port);
            }
            try {
                addr = InetAddress.getByName(hostname);
                hostname = null;
            } catch (UnknownHostException e) {
            }
        } else {
            addr = null;
        }
    }
    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(host, port, false);
    }
    public final int getPort() {
        return port;
    }
    public final InetAddress getAddress() {
        return addr;
    }
    public final String getHostName() {
        return (null != addr) ? addr.getHostName() : hostname;
    }
    public final boolean isUnresolved() {
        return addr == null;
    }
    @Override
    public String toString() {
        String host;
        if (addr != null) {
            host = addr.toString();
        } else {
            host = hostname;
        }
        return host + ":" + port; 
    }
    @Override
    public final boolean equals(Object socketAddr) {
        if (this == socketAddr) {
            return true;
        }
        if (!(socketAddr instanceof InetSocketAddress)) {
            return false;
        }
        InetSocketAddress iSockAddr = (InetSocketAddress) socketAddr;
        if (port != iSockAddr.port) {
            return false;
        }
        if ((addr == null) && (iSockAddr.addr == null)) {
            return hostname.equals(iSockAddr.hostname);
        }
        if (addr == null) {
            return false;
        }
        return addr.equals(iSockAddr.addr);
    }
    @Override
    public final int hashCode() {
        if (addr == null) {
            return hostname.hashCode() + port;
        }
        return addr.hashCode() + port;
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
    }
}
