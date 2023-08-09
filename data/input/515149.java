    value = SelectionKey.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "SelectionKey",
            args = {}
        )
    }
)
public class SelectionKeyTest extends TestCase {
    Selector selector;
    SocketChannel sc;
    SelectionKey selectionKey;
    private static String LOCAL_ADDR = "127.0.0.1";
    protected void setUp() throws Exception {
        super.setUp();
        selector = Selector.open();
        sc = SocketChannel.open();
        sc.configureBlocking(false);
        selectionKey = sc.register(selector, SelectionKey.OP_CONNECT);
    }
    protected void tearDown() throws Exception {
        selectionKey.cancel();
        selectionKey = null;
        selector.close();
        selector = null;
        super.tearDown();
    }
    static class MockSelectionKey extends SelectionKey {
        private int interestOps;
        MockSelectionKey(int ops) {
            interestOps = ops;
        }
        public void cancel() {
        }
        public SelectableChannel channel() {
            return null;
        }
        public int interestOps() {
            return 0;
        }
        public SelectionKey interestOps(int operations) {
            return null;
        }
        public boolean isValid() {
            return true;
        }
        public int readyOps() {
            return interestOps;
        }
        public Selector selector() {
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "attach",
        args = {java.lang.Object.class}
    )
    public void test_attach() {
        MockSelectionKey mockSelectionKey = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        Object o = new Object();
        Object check = mockSelectionKey.attach(o);
        assertNull(check);
        check = mockSelectionKey.attach(null);
        assertSame(o, check);
        check = mockSelectionKey.attach(o);
        assertNull(check);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "attachment",
        args = {}
    )
    public void test_attachment() {
        MockSelectionKey mockSelectionKey = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertNull(mockSelectionKey.attachment());
        Object o = new Object();
        mockSelectionKey.attach(o);
        assertSame(o, mockSelectionKey.attachment());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "channel",
        args = {}
    )
    public void test_channel() {
        assertSame(sc, selectionKey.channel());
        selectionKey.cancel();
        assertSame(sc, selectionKey.channel());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "interestOps",
        args = {}
    )
    public void test_interestOps() {
        assertEquals(SelectionKey.OP_CONNECT, selectionKey.interestOps());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Doesn't verify CancelledKeyException.",
        method = "interestOps",
        args = {int.class}
    )
    public void test_interestOpsI() {
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        assertEquals(SelectionKey.OP_WRITE, selectionKey.interestOps());
        try {
            selectionKey.interestOps(SelectionKey.OP_ACCEPT);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            selectionKey.interestOps(~sc.validOps());
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
        }
        try {
            selectionKey.interestOps(-1);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
        }
        selectionKey.cancel();
        try {
            selectionKey.interestOps(-1);
            fail("should throw IAE.");
        } catch (CancelledKeyException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )
    public void test_isValid() {
        assertTrue(selectionKey.isValid());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )    
    public void test_isValid_KeyCancelled() {
        selectionKey.cancel();
        assertFalse(selectionKey.isValid());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )    
    public void test_isValid_ChannelColsed() throws IOException {
        sc.close();
        assertFalse(selectionKey.isValid());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )    
    public void test_isValid_SelectorClosed() throws IOException {
        selector.close();
        assertFalse(selectionKey.isValid());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAcceptable",
        args = {}
    )
    public void test_isAcceptable() throws IOException {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertTrue(mockSelectionKey1.isAcceptable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_CONNECT);
        assertFalse(mockSelectionKey2.isAcceptable());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isConnectable",
        args = {}
    )
    public void test_isConnectable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_CONNECT);
        assertTrue(mockSelectionKey1.isConnectable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isConnectable());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isReadable",
        args = {}
    )
    public void test_isReadable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_READ);
        assertTrue(mockSelectionKey1.isReadable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isReadable());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isWritable",
        args = {}
    )
    public void test_isWritable() throws Exception {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_WRITE);
        assertTrue(mockSelectionKey1.isWritable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isWritable());
        Selector selector = SelectorProvider.provider().openSelector();
        Pipe pipe = SelectorProvider.provider().openPipe();
        pipe.open();
        pipe.sink().configureBlocking(false);
        SelectionKey key = pipe.sink().register(selector, SelectionKey.OP_WRITE);
        key.cancel();
        try {
            key.isWritable();
            fail("should throw IAE.");
        } catch (CancelledKeyException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "cancel",
        args = {}
    )
    public void test_cancel() {
        selectionKey.cancel();
        try {
            selectionKey.isAcceptable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
        try {
            selectionKey.isConnectable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
        try {
            selectionKey.isReadable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
        try {
            selectionKey.isWritable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
        try {
            selectionKey.readyOps();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
        try {
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
        try {
            selectionKey.interestOps();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readyOps",
        args = {}
    )
    public void test_readyOps() throws IOException {
        int port = Support_PortManager.getNextPort();
        ServerSocket ss = new ServerSocket(port);
        try {
            sc.connect(new InetSocketAddress(LOCAL_ADDR, port));
            assertEquals(0, selectionKey.readyOps());
            assertFalse(selectionKey.isConnectable());
            selector.select();
            assertEquals(SelectionKey.OP_CONNECT, selectionKey.readyOps());
        } finally {
            ss.close();
            ss = null;
        }
        selectionKey.cancel();
        try {
            selectionKey.readyOps();
            fail("should throw IAE.");
        } catch (CancelledKeyException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "selector",
        args = {}
    )
    public void test_selector() {
        assertSame(selector, selectionKey.selector());
        selectionKey.cancel();
        assertSame(selector, selectionKey.selector());
    }
}
