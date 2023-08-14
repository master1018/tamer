public abstract class ServerSocketChannel extends AbstractSelectableChannel {
    protected ServerSocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }
    public static ServerSocketChannel open() throws IOException {
        return SelectorProvider.provider().openServerSocketChannel();
    }
    @Override
    public final int validOps() {
        return SelectionKey.OP_ACCEPT;
    }
    public abstract ServerSocket socket();
    public abstract SocketChannel accept() throws IOException;
}
