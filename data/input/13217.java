class DualStackPlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl
{
    static JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
    protected void datagramSocketCreate() throws SocketException {
        if (fd == null)
            throw new SocketException("Socket closed");
        int newfd = socketCreate(false );
        fdAccess.set(fd, newfd);
    }
    protected synchronized void bind0(int lport, InetAddress laddr)
        throws SocketException {
        int nativefd = checkAndReturnNativeFD();
        if (laddr == null)
            throw new NullPointerException("argument address");
        socketBind(nativefd, laddr, lport);
        if (lport == 0) {
            localPort = socketLocalPort(nativefd);
        } else {
            localPort = lport;
        }
    }
    protected synchronized int peek(InetAddress address) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (address == null)
            throw new NullPointerException("Null address in peek()");
        DatagramPacket peekPacket = new DatagramPacket(new byte[1], 1);
        int peekPort = peekData(peekPacket);
        address = peekPacket.getAddress();
        return peekPort;
    }
    protected synchronized int peekData(DatagramPacket p) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (p == null)
            throw new NullPointerException("packet");
        if (p.getData() == null)
            throw new NullPointerException("packet buffer");
        return socketReceiveOrPeekData(nativefd, p, timeout, connected, true );
    }
    protected synchronized void receive0(DatagramPacket p) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (p == null)
            throw new NullPointerException("packet");
        if (p.getData() == null)
            throw new NullPointerException("packet buffer");
        socketReceiveOrPeekData(nativefd, p, timeout, connected, false );
    }
    protected void send(DatagramPacket p) throws IOException {
        int nativefd = checkAndReturnNativeFD();
        if (p == null)
            throw new NullPointerException("null packet");
        if (p.getAddress() == null ||p.getData() ==null)
            throw new NullPointerException("null address || null buffer");
        socketSend(nativefd, p.getData(), p.getOffset(), p.getLength(),
                   p.getAddress(), p.getPort(), connected);
    }
    protected void connect0(InetAddress address, int port) throws SocketException {
        int nativefd = checkAndReturnNativeFD();
        if (address == null)
            throw new NullPointerException("address");
        socketConnect(nativefd, address, port);
    }
    protected void disconnect0(int family ) {
        if (fd == null || !fd.valid())
            return;   
        socketDisconnect(fdAccess.get(fd));
    }
    protected void datagramSocketClose() {
        if (fd == null || !fd.valid())
            return;   
        socketClose(fdAccess.get(fd));
        fdAccess.set(fd, -1);
    }
    protected void socketSetOption(int opt, Object val) throws SocketException {
        int nativefd = checkAndReturnNativeFD();
        int optionValue = 0;
        switch(opt) {
            case IP_TOS :
            case SO_RCVBUF :
            case SO_SNDBUF :
                optionValue = ((Integer)val).intValue();
                break;
            case SO_REUSEADDR :
            case SO_BROADCAST :
                optionValue = ((Boolean)val).booleanValue() ? 1 : 0;
                break;
            default: 
                throw new SocketException("Option not supported");
        }
        socketSetIntOption(nativefd, opt, optionValue);
    }
    protected Object socketGetOption(int opt) throws SocketException {
        int nativefd = checkAndReturnNativeFD();
        if (opt == SO_BINDADDR) {
            return socketLocalAddress(nativefd);
        }
        int value = socketGetIntOption(nativefd, opt);
        Object returnValue = null;
        switch (opt) {
            case SO_REUSEADDR :
            case SO_BROADCAST :
                returnValue =  (value == 0) ? Boolean.FALSE : Boolean.TRUE;
                break;
            case IP_TOS :
            case SO_RCVBUF :
            case SO_SNDBUF :
                returnValue = new Integer(value);
                break;
            default: 
                throw new SocketException("Option not supported");
        }
        return returnValue;
    }
    protected void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException {
        throw new IOException("Method not implemented!");
    }
    protected void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException {
        throw new IOException("Method not implemented!");
    }
    protected void setTimeToLive(int ttl) throws IOException {
        throw new IOException("Method not implemented!");
    }
    protected int getTimeToLive() throws IOException {
        throw new IOException("Method not implemented!");
    }
    protected void setTTL(byte ttl) throws IOException {
        throw new IOException("Method not implemented!");
    }
    protected byte getTTL() throws IOException {
        throw new IOException("Method not implemented!");
    }
    private int checkAndReturnNativeFD() throws SocketException {
        if (fd == null || !fd.valid())
            throw new SocketException("Socket closed");
        return fdAccess.get(fd);
    }
    private static native void initIDs();
    private static native int socketCreate(boolean v6Only);
    private static native void socketBind(int fd, InetAddress localAddress, int localport)
        throws SocketException;
    private static native void socketConnect(int fd, InetAddress address, int port)
        throws SocketException;
    private static native void socketDisconnect(int fd);
    private static native void socketClose(int fd);
    private static native int socketLocalPort(int fd) throws SocketException;
    private static native Object socketLocalAddress(int fd) throws SocketException;
    private static native int socketReceiveOrPeekData(int fd, DatagramPacket packet,
        int timeout, boolean connected, boolean peek) throws IOException;
    private static native void socketSend(int fd, byte[] data, int offset, int length,
        InetAddress address, int port, boolean connected) throws IOException;
    private static native void socketSetIntOption(int fd, int cmd,
        int optionValue) throws SocketException;
    private static native int socketGetIntOption(int fd, int cmd) throws SocketException;
}
