public abstract class SelectableChannel extends AbstractInterruptibleChannel
        implements Channel {
    protected SelectableChannel() {
        super();
    }
    public abstract Object blockingLock();
    public abstract SelectableChannel configureBlocking(boolean block)
            throws IOException;
    public abstract boolean isBlocking();
    public abstract boolean isRegistered();
    public abstract SelectionKey keyFor(Selector sel);
    public abstract SelectorProvider provider();
    public final SelectionKey register(Selector selector, int operations)
            throws ClosedChannelException {
        return register(selector, operations, null);
    }
    public abstract SelectionKey register(Selector sel, int ops, Object att)
            throws ClosedChannelException;
    public abstract int validOps();
}
