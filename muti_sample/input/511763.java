public class SelectorProviderImpl extends SelectorProvider {
    public SelectorProviderImpl() {
        super();
    }
    public DatagramChannel openDatagramChannel() throws IOException {
        return new DatagramChannelImpl(this);
    }
    public Pipe openPipe() throws IOException {
        return new PipeImpl();
    }
    public AbstractSelector openSelector() throws IOException {
        return new SelectorImpl(this);
    }
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        return new ServerSocketChannelImpl(this);
    }
    public SocketChannel openSocketChannel() throws IOException {
        return new SocketChannelImpl(this);
    }
}
