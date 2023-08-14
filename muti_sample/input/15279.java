public abstract class SctpMultiChannel
    extends AbstractSelectableChannel
{
    protected SctpMultiChannel(SelectorProvider provider) {
        super(provider);
    }
    public static SctpMultiChannel open() throws
        IOException {
        return new sun.nio.ch.SctpMultiChannelImpl((SelectorProvider)null);
    }
    public abstract Set<Association> associations()
        throws IOException;
    public abstract SctpMultiChannel bind(SocketAddress local,
                                          int backlog)
        throws IOException;
    public final SctpMultiChannel bind(SocketAddress local)
        throws IOException {
        return bind(local, 0);
    }
    public abstract SctpMultiChannel bindAddress(InetAddress address)
         throws IOException;
    public abstract SctpMultiChannel unbindAddress(InetAddress address)
         throws IOException;
    public abstract Set<SocketAddress> getAllLocalAddresses()
        throws IOException;
    public abstract Set<SocketAddress> getRemoteAddresses(Association association)
        throws IOException;
    public abstract SctpMultiChannel shutdown(Association association)
            throws IOException;
    public abstract <T> T getOption(SctpSocketOption<T> name,
                                    Association association)
        throws IOException;
    public abstract <T> SctpMultiChannel setOption(SctpSocketOption<T> name,
                                                   T value,
                                                   Association association)
         throws IOException;
    public abstract Set<SctpSocketOption<?>> supportedOptions();
    @Override
    public final int validOps() {
        return (SelectionKey.OP_READ |
                SelectionKey.OP_WRITE );
    }
    public abstract <T> MessageInfo receive(ByteBuffer buffer,
                                            T attachment,
                                            NotificationHandler<T> handler)
        throws IOException;
    public abstract int send(ByteBuffer buffer, MessageInfo messageInfo)
        throws IOException;
    public abstract SctpChannel branch(Association association)
        throws IOException;
}
