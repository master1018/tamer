public class LocalServerSocket {
    private final LocalSocketImpl impl;
    private final LocalSocketAddress localAddress;
    private static final int LISTEN_BACKLOG = 50;
    public LocalServerSocket(String name) throws IOException
    {
        impl = new LocalSocketImpl();
        impl.create(true);
        localAddress = new LocalSocketAddress(name);
        impl.bind(localAddress);
        impl.listen(LISTEN_BACKLOG);
    }
    public LocalServerSocket(FileDescriptor fd) throws IOException
    {
        impl = new LocalSocketImpl(fd);
        impl.listen(LISTEN_BACKLOG);
        localAddress = impl.getSockAddress();
    }
    public LocalSocketAddress getLocalSocketAddress()
    {
        return localAddress;
    }
    public LocalSocket accept() throws IOException
    {
        LocalSocketImpl acceptedImpl = new LocalSocketImpl();
        impl.accept (acceptedImpl);
        return new LocalSocket(acceptedImpl);
    }
    public FileDescriptor getFileDescriptor() {
        return impl.getFileDescriptor();
    }
    public void close() throws IOException
    {
        impl.close();
    }
}
