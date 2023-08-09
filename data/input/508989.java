public abstract class SocketChannel extends AbstractSelectableChannel implements
        ByteChannel, ScatteringByteChannel, GatheringByteChannel {
    protected SocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }
    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }
    public static SocketChannel open(SocketAddress address) throws IOException {
        SocketChannel socketChannel = open();
        if (null != socketChannel) {
            socketChannel.connect(address);
        }
        return socketChannel;
    }
    @Override
    public final int validOps() {
        return (SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
    public abstract Socket socket();
    public abstract boolean isConnected();
    public abstract boolean isConnectionPending();
    public abstract boolean connect(SocketAddress address) throws IOException;
    public abstract boolean finishConnect() throws IOException;
    public abstract int read(ByteBuffer target) throws IOException;
    public abstract long read(ByteBuffer[] targets, int offset, int length)
            throws IOException;
    public synchronized final long read(ByteBuffer[] targets)
            throws IOException {
        return read(targets, 0, targets.length);
    }
    public abstract int write(ByteBuffer source) throws IOException;
    public abstract long write(ByteBuffer[] sources, int offset, int length)
            throws IOException;
    public synchronized final long write(ByteBuffer[] sources)
            throws IOException {
        return write(sources, 0, sources.length);
    }
}
