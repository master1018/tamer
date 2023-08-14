public class MockAbstractSelector extends AbstractSelector {
    class MockSelectionKey extends AbstractSelectionKey {
        boolean cancelled = false;
        Selector selector;
        SelectableChannel channel;
        MockSelectionKey(Selector sel, SelectableChannel chan) {
            selector = sel;
            channel = chan;
        }
        @Override
        public SelectableChannel channel() {
            return channel;
        }
        @Override
        public int interestOps() {
            return 0;
        }
        @Override
        public SelectionKey interestOps(int operations) {
            return null;
        }
        @Override
        public int readyOps() {
            return 0;
        }
        @Override
        public Selector selector() {
            return selector;
        }
    }
    public boolean isImplCloseSelectorCalled = false;
    private Set<SelectionKey> keys = new HashSet<SelectionKey>();
    public boolean isRegisterCalled = false;
    public MockAbstractSelector(SelectorProvider arg0) {
        super(arg0);
    }
    public static MockAbstractSelector openSelector() {
        return new MockAbstractSelector(SelectorProvider.provider());
    }
    public Set<SelectionKey> getCancelledKeys() {
        return super.cancelledKeys();
    }
    protected void implCloseSelector() throws IOException {
        isImplCloseSelectorCalled = true;
    }
    protected SelectionKey register(AbstractSelectableChannel arg0, int arg1,
            Object arg2) {
        isRegisterCalled = true;
        SelectionKey key = new MockSelectionKey(this, arg0);
        keys.add(key);
        return key;
    }
    public void superBegin() {
        super.begin();
    }
    public void superEnd() {
        super.end();
    }
    public void mockDeregister(AbstractSelectionKey key) {
        super.deregister(key);
    }
    public Set<SelectionKey> keys() {
        return keys;
    }
    public Set<SelectionKey> selectedKeys() {
        return null;
    }
    public int selectNow() throws IOException {
        return 0;
    }
    public int select(long arg0) throws IOException {
        return 0;
    }
    public int select() throws IOException {
        return 0;
    }
    public Selector wakeup() {
        return null;
    }
}
