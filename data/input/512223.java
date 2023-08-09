public abstract class SocketImpl implements SocketOptions {
    protected InetAddress address;
    protected int port;
    protected FileDescriptor fd;
    protected int localport;
    INetworkSystem netImpl;
    boolean streaming = true;
    boolean shutdownInput;
    public SocketImpl() {
        this.netImpl = Platform.getNetworkSystem();
    }
    protected abstract void accept(SocketImpl newSocket) throws IOException;
    protected abstract int available() throws IOException;
    protected abstract void bind(InetAddress address, int port)
            throws IOException;
    protected abstract void close() throws IOException;
    protected abstract void connect(String host, int port) throws IOException;
    protected abstract void connect(InetAddress address, int port)
            throws IOException;
    protected abstract void create(boolean isStreaming) throws IOException;
    protected FileDescriptor getFileDescriptor() {
        return fd;
    }
    protected InetAddress getInetAddress() {
        return address;
    }
    protected abstract InputStream getInputStream() throws IOException;
    protected int getLocalPort() {
        return localport;
    }
    public abstract Object getOption(int optID) throws SocketException;
    protected abstract OutputStream getOutputStream() throws IOException;
    protected int getPort() {
        return port;
    }
    protected abstract void listen(int backlog) throws IOException;
    public abstract void setOption(int optID, Object val)
            throws SocketException;
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return new StringBuilder(100).append("Socket[addr=").append(
                getInetAddress()).append(",port=").append(port).append(
                ",localport=").append(getLocalPort()).append("]").toString();
    }
    int write(byte[] buffer, int offset, int count) throws IOException {
        if (!streaming) {
            return this.netImpl.sendDatagram2(fd, buffer, offset, count, port,
                    address);
        }
        return this.netImpl.write(fd, buffer, offset, count);
    }
    protected void shutdownInput() throws IOException {
        throw new IOException(Msg.getString("KA025"));
    }
    protected void shutdownOutput() throws IOException {
        throw new IOException(Msg.getString("KA025"));
    }
    protected abstract void connect(SocketAddress remoteAddr, int timeout)
            throws IOException;
    protected boolean supportsUrgentData() {
        return false;
    }
    protected abstract void sendUrgentData(int value) throws IOException;
    protected void setPerformancePreferences(int connectionTime, int latency,
            int bandwidth) {
    }
}
