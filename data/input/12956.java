public final class Secrets {
    private Secrets() { }
    private static SelectorProvider provider() {
        SelectorProvider p = SelectorProvider.provider();
        if (!(p instanceof SelectorProviderImpl))
            throw new UnsupportedOperationException();
        return p;
    }
    public static SocketChannel newSocketChannel(FileDescriptor fd) {
        try {
            return new SocketChannelImpl(provider(), fd, false);
        } catch (IOException ioe) {
            throw new AssertionError(ioe);
        }
    }
    public static ServerSocketChannel newServerSocketChannel(FileDescriptor fd) {
        try {
            return new ServerSocketChannelImpl(provider(), fd, false);
        } catch (IOException ioe) {
            throw new AssertionError(ioe);
        }
    }
}
