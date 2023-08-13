final class SelectionKeyImpl extends AbstractSelectionKey {
    private AbstractSelectableChannel channel;
    private int interestOps;
    private int readyOps;
    private SelectorImpl selector;
    public SelectionKeyImpl(AbstractSelectableChannel channel, int operations,
            Object attachment, SelectorImpl selector) {
        this.channel = channel;
        interestOps = operations;
        this.selector = selector;
        attach(attachment);
    }
    @Override
    public SelectableChannel channel() {
        return channel;
    }
    @Override
    public int interestOps() {
        checkValid();
        synchronized (selector.keysLock) {
            return interestOps;
        }
    }
    int interestOpsNoCheck() {
        synchronized (selector.keysLock) {
            return interestOps;
        }
    }
    @Override
    public SelectionKey interestOps(int operations) {
        checkValid();
        if ((operations & ~(channel().validOps())) != 0) {
            throw new IllegalArgumentException();
        }
        synchronized (selector.keysLock) {
            interestOps = operations;
        }
        return this;
    }
    @Override
    public int readyOps() {
        checkValid();
        return readyOps;
    }
    @Override
    public Selector selector() {
        return selector;
    }
    void setReadyOps(int readyOps) {
        this.readyOps = readyOps;
    }
    private void checkValid() {
        if (!isValid()) {
            throw new CancelledKeyException();
        }
    }
    boolean isConnected() {
        return !(channel instanceof SocketChannel)
                || ((SocketChannel) channel).isConnected();
    }
}
