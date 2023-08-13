class PlainSocketImpl extends AbstractPlainSocketImpl
{
    static {
        initProto();
    }
    PlainSocketImpl() { }
    PlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }
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
    static native void initProto();
    native void socketSetOption(int cmd, boolean on, Object value)
        throws SocketException;
    native int socketGetOption(int opt, Object iaContainerObj) throws SocketException;
    native void socketSendUrgentData(int data) throws IOException;
}
