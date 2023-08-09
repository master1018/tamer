abstract class BaseSSLSocketImpl extends SSLSocket {
    final Socket self;
    BaseSSLSocketImpl() {
        super();
        this.self = this;
    }
    BaseSSLSocketImpl(Socket socket) {
        super();
        this.self = socket;
    }
    private final static String PROP_NAME =
                                "com.sun.net.ssl.requireCloseNotify";
    final static boolean requireCloseNotify =
                                Debug.getBooleanProperty(PROP_NAME, false);
    public final SocketChannel getChannel() {
        if (self == this) {
            return super.getChannel();
        } else {
            return self.getChannel();
        }
    }
    public void bind(SocketAddress bindpoint) throws IOException {
        if (self == this) {
            super.bind(bindpoint);
        } else {
            throw new IOException(
                "Underlying socket should already be connected");
        }
    }
    public SocketAddress getLocalSocketAddress() {
        if (self == this) {
            return super.getLocalSocketAddress();
        } else {
            return self.getLocalSocketAddress();
        }
    }
    public SocketAddress getRemoteSocketAddress() {
        if (self == this) {
            return super.getRemoteSocketAddress();
        } else {
            return self.getRemoteSocketAddress();
        }
    }
    public final void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }
    public final boolean isConnected() {
        if (self == this) {
            return super.isConnected();
        } else {
            return self.isConnected();
        }
    }
    public final boolean isBound() {
        if (self == this) {
            return super.isBound();
        } else {
            return self.isBound();
        }
    }
    public final void shutdownInput() throws IOException {
        throw new UnsupportedOperationException("The method shutdownInput()" +
                   " is not supported in SSLSocket");
    }
    public final void shutdownOutput() throws IOException {
        throw new UnsupportedOperationException("The method shutdownOutput()" +
                   " is not supported in SSLSocket");
    }
    public final boolean isInputShutdown() {
        if (self == this) {
            return super.isInputShutdown();
        } else {
            return self.isInputShutdown();
        }
    }
    public final boolean isOutputShutdown() {
        if (self == this) {
            return super.isOutputShutdown();
        } else {
            return self.isOutputShutdown();
        }
    }
    protected final void finalize() throws Throwable {
        try {
            close();
        } catch (IOException e1) {
            try {
                if (self == this) {
                    super.close();
                }
            } catch (IOException e2) {
            }
        } finally {
            super.finalize();
        }
    }
    public final InetAddress getInetAddress() {
        if (self == this) {
            return super.getInetAddress();
        } else {
            return self.getInetAddress();
        }
    }
    public final InetAddress getLocalAddress() {
        if (self == this) {
            return super.getLocalAddress();
        } else {
            return self.getLocalAddress();
        }
    }
    public final int getPort() {
        if (self == this) {
            return super.getPort();
        } else {
            return self.getPort();
        }
    }
    public final int getLocalPort() {
        if (self == this) {
            return super.getLocalPort();
        } else {
            return self.getLocalPort();
        }
    }
    public final void setTcpNoDelay(boolean value) throws SocketException {
        if (self == this) {
            super.setTcpNoDelay(value);
        } else {
            self.setTcpNoDelay(value);
        }
    }
    public final boolean getTcpNoDelay() throws SocketException {
        if (self == this) {
            return super.getTcpNoDelay();
        } else {
            return self.getTcpNoDelay();
        }
    }
    public final void setSoLinger(boolean flag, int linger)
            throws SocketException {
        if (self == this) {
            super.setSoLinger(flag, linger);
        } else {
            self.setSoLinger(flag, linger);
        }
    }
    public final int getSoLinger() throws SocketException {
        if (self == this) {
            return super.getSoLinger();
        } else {
            return self.getSoLinger();
        }
    }
    public final void sendUrgentData(int data) throws SocketException {
        throw new SocketException("This method is not supported "
                        + "by SSLSockets");
    }
    public final void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("This method is ineffective, since"
                + " sending urgent data is not supported by SSLSockets");
    }
    public final boolean getOOBInline() throws SocketException {
        throw new SocketException("This method is ineffective, since"
                + " sending urgent data is not supported by SSLSockets");
    }
    public final int getSoTimeout() throws SocketException {
        if (self == this) {
            return super.getSoTimeout();
        } else {
            return self.getSoTimeout();
        }
    }
    public final void setSendBufferSize(int size) throws SocketException {
        if (self == this) {
            super.setSendBufferSize(size);
        } else {
            self.setSendBufferSize(size);
        }
    }
    public final int getSendBufferSize() throws SocketException {
        if (self == this) {
            return super.getSendBufferSize();
        } else {
            return self.getSendBufferSize();
        }
    }
    public final void setReceiveBufferSize(int size) throws SocketException {
        if (self == this) {
            super.setReceiveBufferSize(size);
        } else {
            self.setReceiveBufferSize(size);
        }
    }
    public final int getReceiveBufferSize() throws SocketException {
        if (self == this) {
            return super.getReceiveBufferSize();
        } else {
            return self.getReceiveBufferSize();
        }
    }
    public final void setKeepAlive(boolean on) throws SocketException {
        if (self == this) {
            super.setKeepAlive(on);
        } else {
            self.setKeepAlive(on);
        }
    }
    public final boolean getKeepAlive() throws SocketException {
        if (self == this) {
            return super.getKeepAlive();
        } else {
            return self.getKeepAlive();
        }
    }
    public final void setTrafficClass(int tc) throws SocketException {
        if (self == this) {
            super.setTrafficClass(tc);
        } else {
            self.setTrafficClass(tc);
        }
    }
    public final int getTrafficClass() throws SocketException {
        if (self == this) {
            return super.getTrafficClass();
        } else {
            return self.getTrafficClass();
        }
    }
    public final void setReuseAddress(boolean on) throws SocketException {
        if (self == this) {
            super.setReuseAddress(on);
        } else {
            self.setReuseAddress(on);
        }
    }
    public final boolean getReuseAddress() throws SocketException {
        if (self == this) {
            return super.getReuseAddress();
        } else {
            return self.getReuseAddress();
        }
    }
    public void setPerformancePreferences(int connectionTime,
            int latency, int bandwidth) {
        if (self == this) {
            super.setPerformancePreferences(
                connectionTime, latency, bandwidth);
        } else {
            self.setPerformancePreferences(
                connectionTime, latency, bandwidth);
        }
    }
}
