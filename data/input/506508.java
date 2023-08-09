class LocalSocketImpl
{
    private SocketInputStream fis;
    private SocketOutputStream fos;
    private Object readMonitor = new Object();
    private Object writeMonitor = new Object();
    private FileDescriptor fd;
    FileDescriptor[] inboundFileDescriptors;
    FileDescriptor[] outboundFileDescriptors;
    class SocketInputStream extends InputStream {
        @Override
        public int available() throws IOException {
            return available_native(fd);
        }
        @Override
        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }
        @Override
        public int read() throws IOException {
            int ret;
            synchronized (readMonitor) {
                FileDescriptor myFd = fd;
                if (myFd == null) throw new IOException("socket closed");
                ret = read_native(myFd);
                return ret;
            }
        }
        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            synchronized (readMonitor) {
                FileDescriptor myFd = fd;
                if (myFd == null) throw new IOException("socket closed");
                if (off < 0 || len < 0 || (off + len) > b.length ) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                int ret = readba_native(b, off, len, myFd);
                return ret;
            }
        }
    }
    class SocketOutputStream extends OutputStream {
        @Override
        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }
        @Override
        public void write (byte[] b) throws IOException {
            write(b, 0, b.length);
        }
        @Override
        public void write (byte[] b, int off, int len) throws IOException {
            synchronized (writeMonitor) {
                FileDescriptor myFd = fd;
                if (myFd == null) throw new IOException("socket closed");
                if (off < 0 || len < 0 || (off + len) > b.length ) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                writeba_native(b, off, len, myFd);
            }
        }
        @Override
        public void write (int b) throws IOException {
            synchronized (writeMonitor) {
                FileDescriptor myFd = fd;
                if (myFd == null) throw new IOException("socket closed");
                write_native(b, myFd);
            }
        }
    }
    private native int available_native(FileDescriptor fd) throws IOException;
    private native void close_native(FileDescriptor fd) throws IOException;
    private native int read_native(FileDescriptor fd) throws IOException;
    private native int readba_native(byte[] b, int off, int len,
            FileDescriptor fd) throws IOException;
    private native void writeba_native(byte[] b, int off, int len,
            FileDescriptor fd) throws IOException;
    private native void write_native(int b, FileDescriptor fd)
            throws IOException;
    private native void connectLocal(FileDescriptor fd, String name,
            int namespace) throws IOException;
    private native void bindLocal(FileDescriptor fd, String name, int namespace)
            throws IOException;
    private native FileDescriptor create_native(boolean stream)
            throws IOException;
    private native void listen_native(FileDescriptor fd, int backlog)
            throws IOException;
    private native void shutdown(FileDescriptor fd, boolean shutdownInput);
    private native Credentials getPeerCredentials_native(
            FileDescriptor fd) throws IOException;
    private native int getOption_native(FileDescriptor fd, int optID)
            throws IOException;
    private native void setOption_native(FileDescriptor fd, int optID,
            int b, int value) throws IOException;
    private native FileDescriptor accept
            (FileDescriptor fd, LocalSocketImpl s) throws IOException;
     LocalSocketImpl()
    {
    }
     LocalSocketImpl(FileDescriptor fd) throws IOException
    {
        this.fd = fd;
    }
    public String toString() {
        return super.toString() + " fd:" + fd;
    }
    public void create (boolean stream) throws IOException {
        if (fd == null) {
            fd = create_native(stream);
        }
    }
    public void close() throws IOException {
        synchronized (LocalSocketImpl.this) {
            if (fd == null) return;
            close_native(fd);
            fd = null;
        }
    }
    protected void connect(LocalSocketAddress address, int timeout)
                        throws IOException
    {        
        if (fd == null) {
            throw new IOException("socket not created");
        }
        connectLocal(fd, address.getName(), address.getNamespace().getId());
    }
    public void bind(LocalSocketAddress endpoint) throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        bindLocal(fd, endpoint.getName(), endpoint.getNamespace().getId());
    }
    protected void listen(int backlog) throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        listen_native(fd, backlog);
    }
    protected void accept(LocalSocketImpl s) throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        s.fd = accept(fd, s);
    }
    protected InputStream getInputStream() throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        synchronized (this) {
            if (fis == null) {
                fis = new SocketInputStream();
            }
            return fis;
        }
    }
    protected OutputStream getOutputStream() throws IOException
    { 
        if (fd == null) {
            throw new IOException("socket not created");
        }
        synchronized (this) {
            if (fos == null) {
                fos = new SocketOutputStream();
            }
            return fos;
        }
    }
    protected int available() throws IOException
    {
        return getInputStream().available();
    }
    protected void shutdownInput() throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        shutdown(fd, true);
    }
    protected void shutdownOutput() throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        shutdown(fd, false);
    }
    protected FileDescriptor getFileDescriptor()
    {
        return fd;
    }
    protected boolean supportsUrgentData()
    {
        return false;
    }
    protected void sendUrgentData(int data) throws IOException
    {
        throw new RuntimeException ("not impled");
    }
    public Object getOption(int optID) throws IOException
    {
        if (fd == null) {
            throw new IOException("socket not created");
        }
        if (optID == SocketOptions.SO_TIMEOUT) {
            return 0;
        }
        int value = getOption_native(fd, optID);
        switch (optID)
        {
            case SocketOptions.SO_RCVBUF:
            case SocketOptions.SO_SNDBUF:
                return value;
            case SocketOptions.SO_REUSEADDR:
            default:
                return value;
        }
    }
    public void setOption(int optID, Object value)
            throws IOException {
        int boolValue = -1;
        int intValue = 0;
        if (fd == null) {
            throw new IOException("socket not created");
        }
        if (value instanceof Integer) {
            intValue = (Integer)value;
        } else if (value instanceof Boolean) {
            boolValue = ((Boolean) value)? 1 : 0;
        } else {
            throw new IOException("bad value: " + value);
        }
        setOption_native(fd, optID, boolValue, intValue);
    }
    public void setFileDescriptorsForSend(FileDescriptor[] fds) {
        synchronized(writeMonitor) {
            outboundFileDescriptors = fds;
        }
    }
    public FileDescriptor[] getAncillaryFileDescriptors() throws IOException {
        synchronized(readMonitor) {
            FileDescriptor[] result = inboundFileDescriptors;
            inboundFileDescriptors = null;
            return result;
        }
    }
    public Credentials getPeerCredentials() throws IOException
    {
        return getPeerCredentials_native(fd);
    }
    public LocalSocketAddress getSockAddress() throws IOException
    {
        return null;
    }
    @Override
    protected void finalize() throws IOException {
        close();
    }
}
