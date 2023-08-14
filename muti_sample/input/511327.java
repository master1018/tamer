@TestTargetClass(AbstractSelector.class)
public class AbstractSelectorTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "AbstractSelector",
            args = {SelectorProvider.class}
        )
    })
    public void test_provider() throws IOException {
        Selector mockSelector = new MockAbstractSelector(SelectorProvider
                .provider());
        assertTrue(mockSelector.isOpen());
        assertSame(SelectorProvider.provider(), mockSelector.provider());
        mockSelector = new MockAbstractSelector(null);
        assertNull(mockSelector.provider());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implCloseSelector",
            args = {}
        )
    })
    public void test_close() throws IOException {
        MockAbstractSelector mockSelector = new MockAbstractSelector(
                SelectorProvider.provider());
        mockSelector.close();
        assertTrue(mockSelector.isImplCloseSelectorCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "begin",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "end",
            args = {}
        )
    })
    public void test_begin_end() throws IOException {
        MockAbstractSelector mockSelector = new MockAbstractSelector(
                SelectorProvider.provider());     
        try {
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
        }
        mockSelector = new MockAbstractSelector(SelectorProvider.provider());
        try {
            mockSelector.superBegin();
            mockSelector.close();
        } finally {
            mockSelector.superEnd();
        }
        try {
            mockSelector.superBegin();
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
        }
        try {
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
            mockSelector.superEnd();
        }
        mockSelector.close();
        try {
            mockSelector.superBegin();
        } finally {
            mockSelector.superEnd();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isOpen",
        args = {}
    )
    public void test_isOpen() throws Exception {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        assertTrue(acceptSelector.isOpen());
        acceptSelector.close();
        assertFalse(acceptSelector.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "AbstractSelector",
        args = {SelectorProvider.class}
    )
    public void test_Constructor_LSelectorProvider() throws Exception {
        Selector acceptSelector = new MockAbstractSelector(
                SelectorProvider.provider());
        assertSame(SelectorProvider.provider(), acceptSelector.provider());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies register method from SelectableChannel class.",
        method = "register",
        args = {AbstractSelectableChannel.class, int.class, java.lang.Object.class}
    )   
    public void test_register_LAbstractSelectableChannelIObject() 
            throws Exception {
        Selector acceptSelector = new MockSelectorProvider().openSelector();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        assertFalse(ssc.isRegistered());
        ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
        assertTrue(ssc.isRegistered());
        assertTrue(((MockAbstractSelector)acceptSelector).isRegisterCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "cancelledKeys",
        args = {}
    )    
    public void test_cancelledKeys() throws Exception {
        MockSelectorProvider prov = new MockSelectorProvider();
        Selector acceptSelector = prov.openSelector();
        SocketChannel sc = prov.openSocketChannel();
        sc.configureBlocking(false);
        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        acceptKey.cancel();
        Set<SelectionKey> cKeys = 
                ((MockAbstractSelector)acceptSelector).getCancelledKeys();
        assertTrue(cKeys.contains(acceptKey));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "deregister",
        args = {AbstractSelectionKey.class}
    )
    public void test_deregister() throws Exception {
        MockSelectorProvider prov = new MockSelectorProvider();
        AbstractSelector acceptSelector = prov.openSelector();
        SocketChannel sc = prov.openSocketChannel();
        sc.configureBlocking(false);
        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        assertTrue(sc.isRegistered());
        assertNotNull(acceptKey);
        ((MockAbstractSelector)acceptSelector).mockDeregister(
                (MockAbstractSelector.MockSelectionKey)acceptKey);
        assertFalse(sc.isRegistered());
    }
    static class MockSelectorProvider extends SelectorProvider {
        private  MockSelectorProvider() {
        }
        @Override
        public DatagramChannel openDatagramChannel() {
            return null;
        }
        @Override
        public Pipe openPipe() {
            return null;
        }
        @Override
        public AbstractSelector openSelector() {
            return new MockAbstractSelector(provider());
        }
        @Override
        public ServerSocketChannel openServerSocketChannel() {
            return null;
        }
        @Override
        public SocketChannel openSocketChannel() throws IOException {
            return SocketChannel.open();
        }
        public static SelectorProvider provider() {
            return new MockSelectorProvider();
        }
    }
}
