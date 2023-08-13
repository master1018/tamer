public class LocalSocket {
    private LocalSocketImpl impl;
    private volatile boolean implCreated;
    private LocalSocketAddress localAddress;
    private boolean isBound;
    private boolean isConnected;
    public LocalSocket() {
        this(new LocalSocketImpl());
        isBound = false;
        isConnected = false;
    }
     LocalSocket(LocalSocketImpl impl) {
        this.impl = impl;
        this.isConnected = false;
        this.isBound = false;
    }
    @Override
    public String toString() {
        return super.toString() + " impl:" + impl;
    }
    private void implCreateIfNeeded() throws IOException {
        if (!implCreated) {
            synchronized (this) {
                if (!implCreated) {
                    implCreated = true;
                    impl.create(true);
                }
            }
        }
    }
    public void connect(LocalSocketAddress endpoint) throws IOException {
        synchronized (this) {
            if (isConnected) {
                throw new IOException("already connected");
            }
            implCreateIfNeeded();
            impl.connect(endpoint, 0);
            isConnected = true;
            isBound = true;
        }
    }
    public void bind(LocalSocketAddress bindpoint) throws IOException {
        implCreateIfNeeded();
        synchronized (this) {
            if (isBound) {
                throw new IOException("already bound");
            }
            localAddress = bindpoint;
            impl.bind(localAddress);
            isBound = true;
        }
    }
    public LocalSocketAddress getLocalSocketAddress() {
        return localAddress;
    }
    public InputStream getInputStream() throws IOException {
        implCreateIfNeeded();
        return impl.getInputStream();
    }
    public OutputStream getOutputStream() throws IOException {
        implCreateIfNeeded();
        return impl.getOutputStream();
    }
    public void close() throws IOException {
        implCreateIfNeeded();
        impl.close();
    }
    public void shutdownInput() throws IOException {
        implCreateIfNeeded();
        impl.shutdownInput();
    }
    public void shutdownOutput() throws IOException {
        implCreateIfNeeded();
        impl.shutdownOutput();
    }
    public void setReceiveBufferSize(int size) throws IOException {
        impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size));
    }
    public int getReceiveBufferSize() throws IOException {
        return ((Integer) impl.getOption(SocketOptions.SO_RCVBUF)).intValue();
    }
    public void setSoTimeout(int n) throws IOException {
        impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(n));
    }
    public int getSoTimeout() throws IOException {
        return ((Integer) impl.getOption(SocketOptions.SO_TIMEOUT)).intValue();
    }
    public void setSendBufferSize(int n) throws IOException {
        impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(n));
    }
    public int getSendBufferSize() throws IOException {
        return ((Integer) impl.getOption(SocketOptions.SO_SNDBUF)).intValue();
    }
    public LocalSocketAddress getRemoteSocketAddress() {
        throw new UnsupportedOperationException();
    }
    public synchronized boolean isConnected() {
        return isConnected;
    }
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }
    public synchronized boolean isBound() {
        return isBound;
    }
    public boolean isOutputShutdown() {
        throw new UnsupportedOperationException();
    }
    public boolean isInputShutdown() {
        throw new UnsupportedOperationException();
    }
    public void connect(LocalSocketAddress endpoint, int timeout)
            throws IOException {
        throw new UnsupportedOperationException();
    }
    public void setFileDescriptorsForSend(FileDescriptor[] fds) {
        impl.setFileDescriptorsForSend(fds);
    }
    public FileDescriptor[] getAncillaryFileDescriptors() throws IOException {
        return impl.getAncillaryFileDescriptors();
    }
    public Credentials getPeerCredentials() throws IOException {
        return impl.getPeerCredentials();
    }
    public FileDescriptor getFileDescriptor() {
        return impl.getFileDescriptor();
    }    
}
