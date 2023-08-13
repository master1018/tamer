public abstract class SctpChannel
    extends AbstractSelectableChannel
{
    protected SctpChannel(SelectorProvider provider) {
        super(provider);
    }
    public static SctpChannel open() throws
        IOException {
        return new sun.nio.ch.SctpChannelImpl((SelectorProvider)null);
    }
    public static SctpChannel open(SocketAddress remote, int maxOutStreams,
                   int maxInStreams) throws IOException {
        SctpChannel ssc = SctpChannel.open();
        ssc.connect(remote, maxOutStreams, maxInStreams);
        return ssc;
    }
    public abstract Association association() throws IOException;
    public abstract SctpChannel bind(SocketAddress local)
        throws IOException;
    public abstract SctpChannel bindAddress(InetAddress address)
         throws IOException;
    public abstract SctpChannel unbindAddress(InetAddress address)
         throws IOException;
    public abstract boolean connect(SocketAddress remote) throws IOException;
    public abstract boolean connect(SocketAddress remote,
                                    int maxOutStreams,
                                    int maxInStreams)
        throws IOException;
    public abstract boolean isConnectionPending();
    public abstract boolean finishConnect() throws IOException;
    public abstract Set<SocketAddress> getAllLocalAddresses()
        throws IOException;
    public abstract Set<SocketAddress> getRemoteAddresses()
        throws IOException;
    public abstract SctpChannel shutdown() throws IOException;
    public abstract <T> T getOption(SctpSocketOption<T> name)
        throws IOException;
    public abstract <T> SctpChannel setOption(SctpSocketOption<T> name, T value)
        throws IOException;
    public abstract Set<SctpSocketOption<?>> supportedOptions();
    @Override
    public final int validOps() {
        return (SelectionKey.OP_READ |
                SelectionKey.OP_WRITE |
                SelectionKey.OP_CONNECT);
    }
    public abstract <T> MessageInfo receive(ByteBuffer dst,
                                            T attachment,
                                            NotificationHandler<T> handler)
        throws IOException;
    public abstract int send(ByteBuffer src, MessageInfo messageInfo)
        throws IOException;
}
