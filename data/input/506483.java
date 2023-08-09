public abstract class AbstractSelector extends Selector {
    private final AtomicBoolean isOpen = new AtomicBoolean(true);
    private SelectorProvider provider = null;
    private Set<SelectionKey> cancelledKeysSet = new HashSet<SelectionKey>();
    protected AbstractSelector(SelectorProvider selectorProvider) {
        provider = selectorProvider;
    }
    @Override
    public final void close() throws IOException {
        if (isOpen.getAndSet(false)) {
            implCloseSelector();
        }
    }
    protected abstract void implCloseSelector() throws IOException;
    @Override
    public final boolean isOpen() {
        return isOpen.get();
    }
    @Override
    public final SelectorProvider provider() {
        return provider;
    }
    protected final Set<SelectionKey> cancelledKeys() {
        return cancelledKeysSet;
    }
    protected abstract SelectionKey register(AbstractSelectableChannel channel,
            int operations, Object attachment);
    protected final void deregister(AbstractSelectionKey key) {
        ((AbstractSelectableChannel) key.channel()).deregister(key);
        key.isValid = false;
    }
    protected final void begin() {
        if (AbstractInterruptibleChannel.setInterruptAction != null) {
            try {
                AbstractInterruptibleChannel.setInterruptAction.invoke(Thread
                        .currentThread(), new Object[] { new Runnable() {
                    public void run() {
                        AbstractSelector.this.wakeup();
                    }
                } });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    protected final void end() {
        if (AbstractInterruptibleChannel.setInterruptAction != null) {
            try {
                AbstractInterruptibleChannel.setInterruptAction.invoke(Thread
                        .currentThread(), new Object[] { null });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    void cancel(SelectionKey key) {
        synchronized (cancelledKeysSet) {
            cancelledKeysSet.add(key);
        }
    }
}
