public abstract class DatagramChannel extends AbstractSelectableChannel
        implements ByteChannel, ScatteringByteChannel, GatheringByteChannel {
    protected DatagramChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }
    public static DatagramChannel open() throws IOException {
        return SelectorProvider.provider().openDatagramChannel();
    }
    @Override
    public final int validOps() {
        return (SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
    public abstract DatagramSocket socket();
    public abstract boolean isConnected();
    public abstract DatagramChannel connect(SocketAddress address)
            throws IOException;
    public abstract DatagramChannel disconnect() throws IOException;
    public abstract SocketAddress receive(ByteBuffer target) throws IOException;
    public abstract int send(ByteBuffer source, SocketAddress address)
            throws IOException;
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
