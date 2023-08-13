public abstract class DatagramSocketImpl implements SocketOptions {
    protected FileDescriptor fd;
    protected int localPort;
    public DatagramSocketImpl() {
        localPort = -1;
    }
    protected abstract void bind(int port, InetAddress addr)
            throws SocketException;
    protected abstract void close();
    protected abstract void create() throws SocketException;
    protected FileDescriptor getFileDescriptor() {
        return fd;
    }
    InetAddress getLocalAddress() {
        return Platform.getNetworkSystem().getSocketLocalAddress(fd);
    }
    protected int getLocalPort() {
        return localPort;
    }
    @Deprecated
    protected abstract byte getTTL() throws IOException;
    protected abstract int getTimeToLive() throws IOException;
    protected abstract void join(InetAddress addr) throws IOException;
    protected abstract void joinGroup(SocketAddress addr,
            NetworkInterface netInterface) throws IOException;
    protected abstract void leave(InetAddress addr) throws IOException;
    protected abstract void leaveGroup(SocketAddress addr,
            NetworkInterface netInterface) throws IOException;
    protected abstract int peek(InetAddress sender) throws IOException;
    protected abstract void receive(DatagramPacket pack) throws IOException;
    protected abstract void send(DatagramPacket pack) throws IOException;
    protected abstract void setTimeToLive(int ttl) throws IOException;
    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;
    protected void connect(InetAddress inetAddr, int port)
            throws SocketException {
    }
    protected void disconnect() {
    }
    protected abstract int peekData(DatagramPacket pack) throws IOException;
}
