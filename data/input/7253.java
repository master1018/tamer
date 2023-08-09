class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl
{
    static {
        init();
    }
    protected synchronized native void bind0(int lport, InetAddress laddr)
        throws SocketException;
    protected native void send(DatagramPacket p) throws IOException;
    protected synchronized native int peek(InetAddress i) throws IOException;
    protected synchronized native int peekData(DatagramPacket p) throws IOException;
    protected synchronized native void receive0(DatagramPacket p)
        throws IOException;
    protected native void setTimeToLive(int ttl) throws IOException;
    protected native int getTimeToLive() throws IOException;
    protected native void setTTL(byte ttl) throws IOException;
    protected native byte getTTL() throws IOException;
    protected native void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;
    protected native void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;
    protected native void datagramSocketCreate() throws SocketException;
    protected native void datagramSocketClose();
    protected native void socketSetOption(int opt, Object val)
        throws SocketException;
    protected native Object socketGetOption(int opt) throws SocketException;
    protected native void connect0(InetAddress address, int port) throws SocketException;
    protected native void disconnect0(int family);
    private native static void init();
}
