class TwoStacksPlainSocketImpl extends AbstractPlainSocketImpl
{
    private FileDescriptor fd1;
    private InetAddress anyLocalBoundAddr = null;
    private int lastfd = -1;
    static {
        initProto();
    }
    public TwoStacksPlainSocketImpl() {}
    public TwoStacksPlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }
    protected synchronized void create(boolean stream) throws IOException {
        fd1 = new FileDescriptor();
        super.create(stream);
    }
    protected synchronized void bind(InetAddress address, int lport)
        throws IOException
    {
        super.bind(address, lport);
        if (address.isAnyLocalAddress()) {
            anyLocalBoundAddr = address;
        }
    }
    public Object getOption(int opt) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        if (opt == SO_BINDADDR) {
            if (fd != null && fd1 != null ) {
                return anyLocalBoundAddr;
            }
            InetAddressContainer in = new InetAddressContainer();
            socketGetOption(opt, in);
            return in.addr;
        } else
            return super.getOption(opt);
    }
    protected void close() throws IOException {
        synchronized(fdLock) {
            if (fd != null || fd1 != null) {
                if (fdUseCount == 0) {
                    if (closePending) {
                        return;
                    }
                    closePending = true;
                    socketClose();
                    fd = null;
                    fd1 = null;
                    return;
                } else {
                    if (!closePending) {
                        closePending = true;
                        fdUseCount--;
                        socketClose();
                    }
                }
            }
        }
    }
    void reset() throws IOException {
        if (fd != null || fd1 != null) {
            socketClose();
        }
        fd = null;
        fd1 = null;
        super.reset();
    }
    public boolean isClosedOrPending() {
        synchronized (fdLock) {
            if (closePending || (fd == null && fd1 == null)) {
                return true;
            } else {
                return false;
            }
        }
    }
    static native void initProto();
    native void socketCreate(boolean isServer) throws IOException;
    native void socketConnect(InetAddress address, int port, int timeout)
        throws IOException;
    native void socketBind(InetAddress address, int port)
        throws IOException;
    native void socketListen(int count) throws IOException;
    native void socketAccept(SocketImpl s) throws IOException;
    native int socketAvailable() throws IOException;
    native void socketClose0(boolean useDeferredClose) throws IOException;
    native void socketShutdown(int howto) throws IOException;
    native void socketSetOption(int cmd, boolean on, Object value)
        throws SocketException;
    native int socketGetOption(int opt, Object iaContainerObj) throws SocketException;
    native void socketSendUrgentData(int data) throws IOException;
}
