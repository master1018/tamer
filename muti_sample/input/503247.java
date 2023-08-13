public abstract class AbstractSelectableChannel extends SelectableChannel {
    private final SelectorProvider provider;
    private List<SelectionKey> keyList = new ArrayList<SelectionKey>();
    static private class BlockingLock {
    }
    private final Object blockingLock = new BlockingLock();
    boolean isBlocking = true;
    protected AbstractSelectableChannel(SelectorProvider selectorProvider) {
        super();
        provider = selectorProvider;
    }
    @Override
    public final SelectorProvider provider() {
        return provider;
    }
    @Override
    synchronized public final boolean isRegistered() {
        return !keyList.isEmpty();
    }
    @Override
    synchronized public final SelectionKey keyFor(Selector selector) {
        for (int i = 0; i < keyList.size(); i++) {
            SelectionKey key = keyList.get(i);
            if (null != key && key.selector() == selector) {
                return key;
            }
        }
        return null;
    }
    @Override
    public final SelectionKey register(Selector selector, int interestSet,
            Object attachment) throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!((interestSet & ~validOps()) == 0)) {
            throw new IllegalArgumentException();
        }
        synchronized (blockingLock) {
            if (isBlocking) {
                throw new IllegalBlockingModeException();
            }
            if (!selector.isOpen()) {
                if (0 == interestSet) {
                    throw new IllegalSelectorException();
                }
                throw new NullPointerException();
            }
            SelectionKey key = keyFor(selector);
            if (null == key) {
                key = ((AbstractSelector) selector).register(this, interestSet,
                        attachment);
                keyList.add(key);
            } else {
                if (!key.isValid()) {
                    throw new CancelledKeyException();
                }
                key.interestOps(interestSet);
                key.attach(attachment);
            }
            return key;
        }
    }
    @Override
    synchronized protected final void implCloseChannel() throws IOException {
        implCloseSelectableChannel();
        for (int i = 0; i < keyList.size(); i++) {
            SelectionKey key = keyList.get(i);
            if (null != key) {
                key.cancel();
            }
        }
    }
    protected abstract void implCloseSelectableChannel() throws IOException;
    @Override
    public final boolean isBlocking() {
        synchronized (blockingLock) {
            return isBlocking;
        }
    }
    @Override
    public final Object blockingLock() {
        return blockingLock;
    }
    @Override
    public final SelectableChannel configureBlocking(boolean blockingMode)
            throws IOException {
        if (isOpen()) {
            synchronized (blockingLock) {
                if (isBlocking == blockingMode) {
                    return this;
                }
                if (blockingMode && containsValidKeys()) {
                    throw new IllegalBlockingModeException();
                }
                implConfigureBlocking(blockingMode);
                isBlocking = blockingMode;
            }
            return this;
        }
        throw new ClosedChannelException();
    }
    protected abstract void implConfigureBlocking(boolean blockingMode)
            throws IOException;
    synchronized void deregister(SelectionKey k) {
        if (null != keyList) {
            keyList.remove(k);
        }
    }
    private synchronized boolean containsValidKeys() {
        for (int i = 0; i < keyList.size(); i++) {
            SelectionKey key = keyList.get(i);
            if (key != null && key.isValid()) {
                return true;
            }
        }
        return false;
    }
}
