abstract class AbstractPlainDatagramSocketImpl extends DatagramSocketImpl
{
    int timeout = 0;
    boolean connected = false;
    private int trafficClass = 0;
    private InetAddress connectedAddress = null;
    private int connectedPort = -1;
    private int multicastInterface = 0;
    private boolean loopbackMode = true;
    private int ttl = -1;
    static {
        java.security.AccessController.doPrivileged(
                  new sun.security.action.LoadLibraryAction("net"));
    }
    protected synchronized void create() throws SocketException {
        fd = new FileDescriptor();
        ResourceManager.beforeUdpCreate();
        try {
            datagramSocketCreate();
        } catch (SocketException ioe) {
            ResourceManager.afterUdpClose();
            fd = null;
            throw ioe;
        }
    }
    protected synchronized void bind(int lport, InetAddress laddr)
        throws SocketException {
        bind0(lport, laddr);
    }
    protected abstract void bind0(int lport, InetAddress laddr)
        throws SocketException;
    protected abstract void send(DatagramPacket p) throws IOException;
    protected void connect(InetAddress address, int port) throws SocketException {
        connect0(address, port);
        connectedAddress = address;
        connectedPort = port;
        connected = true;
    }
    protected void disconnect() {
        disconnect0(connectedAddress.family);
        connected = false;
        connectedAddress = null;
        connectedPort = -1;
    }
    protected abstract int peek(InetAddress i) throws IOException;
    protected abstract int peekData(DatagramPacket p) throws IOException;
    protected synchronized void receive(DatagramPacket p)
        throws IOException {
        receive0(p);
    }
    protected abstract void receive0(DatagramPacket p)
        throws IOException;
    protected abstract void setTimeToLive(int ttl) throws IOException;
    protected abstract int getTimeToLive() throws IOException;
    protected abstract void setTTL(byte ttl) throws IOException;
    protected abstract byte getTTL() throws IOException;
    protected void join(InetAddress inetaddr) throws IOException {
        join(inetaddr, null);
    }
    protected void leave(InetAddress inetaddr) throws IOException {
        leave(inetaddr, null);
    }
    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
        throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        join(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }
    protected abstract void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
        throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        leave(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }
    protected abstract void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;
    protected void close() {
        if (fd != null) {
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            fd = null;
        }
    }
    protected boolean isClosed() {
        return (fd == null) ? true : false;
    }
    protected void finalize() {
        close();
    }
     public void setOption(int optID, Object o) throws SocketException {
         if (isClosed()) {
             throw new SocketException("Socket Closed");
         }
         switch (optID) {
         case SO_TIMEOUT:
             if (o == null || !(o instanceof Integer)) {
                 throw new SocketException("bad argument for SO_TIMEOUT");
             }
             int tmp = ((Integer) o).intValue();
             if (tmp < 0)
                 throw new IllegalArgumentException("timeout < 0");
             timeout = tmp;
             return;
         case IP_TOS:
             if (o == null || !(o instanceof Integer)) {
                 throw new SocketException("bad argument for IP_TOS");
             }
             trafficClass = ((Integer)o).intValue();
             break;
         case SO_REUSEADDR:
             if (o == null || !(o instanceof Boolean)) {
                 throw new SocketException("bad argument for SO_REUSEADDR");
             }
             break;
         case SO_BROADCAST:
             if (o == null || !(o instanceof Boolean)) {
                 throw new SocketException("bad argument for SO_BROADCAST");
             }
             break;
         case SO_BINDADDR:
             throw new SocketException("Cannot re-bind Socket");
         case SO_RCVBUF:
         case SO_SNDBUF:
             if (o == null || !(o instanceof Integer) ||
                 ((Integer)o).intValue() < 0) {
                 throw new SocketException("bad argument for SO_SNDBUF or " +
                                           "SO_RCVBUF");
             }
             break;
         case IP_MULTICAST_IF:
             if (o == null || !(o instanceof InetAddress))
                 throw new SocketException("bad argument for IP_MULTICAST_IF");
             break;
         case IP_MULTICAST_IF2:
             if (o == null || !(o instanceof NetworkInterface))
                 throw new SocketException("bad argument for IP_MULTICAST_IF2");
             break;
         case IP_MULTICAST_LOOP:
             if (o == null || !(o instanceof Boolean))
                 throw new SocketException("bad argument for IP_MULTICAST_LOOP");
             break;
         default:
             throw new SocketException("invalid option: " + optID);
         }
         socketSetOption(optID, o);
     }
    public Object getOption(int optID) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        Object result;
        switch (optID) {
            case SO_TIMEOUT:
                result = new Integer(timeout);
                break;
            case IP_TOS:
                result = socketGetOption(optID);
                if ( ((Integer)result).intValue() == -1) {
                    result = new Integer(trafficClass);
                }
                break;
            case SO_BINDADDR:
            case IP_MULTICAST_IF:
            case IP_MULTICAST_IF2:
            case SO_RCVBUF:
            case SO_SNDBUF:
            case IP_MULTICAST_LOOP:
            case SO_REUSEADDR:
            case SO_BROADCAST:
                result = socketGetOption(optID);
                break;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        return result;
    }
    protected abstract void datagramSocketCreate() throws SocketException;
    protected abstract void datagramSocketClose();
    protected abstract void socketSetOption(int opt, Object val)
        throws SocketException;
    protected abstract Object socketGetOption(int opt) throws SocketException;
    protected abstract void connect0(InetAddress address, int port) throws SocketException;
    protected abstract void disconnect0(int family);
}
