public abstract class SctpServerChannel
    extends AbstractSelectableChannel
{
    protected SctpServerChannel(SelectorProvider provider) {
        super(provider);
    }
    public static SctpServerChannel open() throws
        IOException {
        return new sun.nio.ch.SctpServerChannelImpl((SelectorProvider)null);
    }
    public abstract SctpChannel accept() throws IOException;
    public final SctpServerChannel bind(SocketAddress local)
        throws IOException {
        return bind(local, 0);
    }
    public abstract SctpServerChannel bind(SocketAddress local,
                                           int backlog)
        throws IOException;
    public abstract SctpServerChannel bindAddress(InetAddress address)
         throws IOException;
    public abstract SctpServerChannel unbindAddress(InetAddress address)
         throws IOException;
    public abstract Set<SocketAddress> getAllLocalAddresses()
        throws IOException;
    public abstract <T> T getOption(SctpSocketOption<T> name) throws IOException;
    public abstract <T> SctpServerChannel setOption(SctpSocketOption<T> name,
                                                    T value)
        throws IOException;
    public abstract Set<SctpSocketOption<?>> supportedOptions();
    @Override
    public final int validOps() {
        return SelectionKey.OP_ACCEPT;
    }
}
