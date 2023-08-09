public class PlainServerSocketImpl extends PlainSocketImpl {
    public PlainServerSocketImpl() {
        super();
    }
    public PlainServerSocketImpl(FileDescriptor fd) {
        super();
        this.fd = fd;
    }
    @Override
    protected void create(boolean isStreaming) throws SocketException {
        streaming = isStreaming;
        if (isStreaming) {
            netImpl.createServerStreamSocket(fd, NetUtil.preferIPv4Stack());
        } else {
            netImpl.createDatagramSocket(fd, NetUtil.preferIPv4Stack());
        }
    }
}
