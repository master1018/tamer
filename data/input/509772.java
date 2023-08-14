    value = Selector.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "Selector",
            args = {}
        )
    }
)
public class SelectorTest extends TestCase {
    private static final int WAIT_TIME = 100;
    private static final int PORT = Support_PortManager.getNextPort();
    private static final InetSocketAddress LOCAL_ADDRESS = new InetSocketAddress(
            "127.0.0.1", PORT);
    Selector selector;
    ServerSocketChannel ssc;
    enum SelectType {
        NULL, TIMEOUT, NOW
    }
    protected void setUp() throws Exception {
        super.setUp();
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ServerSocket ss = ssc.socket();
        InetSocketAddress address = new InetSocketAddress(PORT);
        ss.bind(address);
        selector = Selector.open();
    }
    protected void tearDown() throws Exception {
        try {
            ssc.close();
        } catch (Exception e) {
        }
        try {
            selector.close();
        } catch (Exception e) {
        }
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "open",
        args = {}
    )
    public void test_open() throws IOException {
        assertNotNull(selector);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isOpen",
        args = {}
    )
    public void test_isOpen() throws IOException {
        assertTrue(selector.isOpen());
        selector.close();
        assertFalse(selector.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "provider",
        args = {}
    )
    public void test_provider() throws IOException {
        assertNotNull(selector.provider());
        assertSame(SelectorProvider.provider(), selector.provider());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keys",
        args = {}
    )
    public void test_keys() throws IOException {
        SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);
        Set<SelectionKey> keySet = selector.keys();
        Set<SelectionKey> keySet2 = selector.keys();
        assertSame(keySet, keySet2);
        assertEquals(1,keySet.size());
        SelectionKey key2 = keySet.iterator().next();
        assertEquals(key,key2);
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey key3 = sc.register(selector, SelectionKey.OP_READ);
        try {
            keySet2.add(key3);
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        try {
            keySet2.remove(key3);
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        try {
            keySet2.clear();
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        selector.close();
        try {
            selector.keys();
            fail("should throw ClosedSelectorException");
        } catch (ClosedSelectorException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "selectedKeys",
        args = {}
    )
    public void test_selectedKeys() throws IOException {
        SocketChannel sc = SocketChannel.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        try {
            int count = 0;
            sc.connect(LOCAL_ADDRESS);
            count = blockingSelect(SelectType.NULL, 0);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Set<SelectionKey> selectedKeys2 = selector.selectedKeys();
            assertSame(selectedKeys, selectedKeys2);
            assertEquals(1, selectedKeys.size());
            assertEquals(ssc.keyFor(selector), selectedKeys.iterator().next());
            try {
                selectedKeys.add(ssc.keyFor(selector));
                fail("Should throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
            }
            selectedKeys.clear();
            Set<SelectionKey> selectedKeys3 = selector.selectedKeys();
            assertSame(selectedKeys, selectedKeys3);
            ssc.keyFor(selector).cancel();
            assertEquals(0, selectedKeys.size());
            selector.close();
            try {
                selector.selectedKeys();
                fail("should throw ClosedSelectorException");
            } catch (ClosedSelectorException e) {
            }
        } finally {
            sc.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies selectNow() method for Selector registered with SelectionKeys.OP_ACCEPT,  SelectionKeys.OP_CONNECT, SelectionKeys.OP_READ, SelectionKeys.OP_WRITE keys.",
        method = "selectNow",
        args = {}
    )
    public void test_selectNow() throws IOException {
        assert_select_OP_ACCEPT(SelectType.NOW, 0);
        assert_select_OP_CONNECT(SelectType.NOW, 0);
        assert_select_OP_READ(SelectType.NOW, 0);
        assert_select_OP_WRITE(SelectType.NOW, 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that ClosedSelectorException is thrown if selectNow() method is called for closed Selector.",
        method = "selectNow",
        args = {}
    )
    public void test_selectNow_SelectorClosed() throws IOException {
        assert_select_SelectorClosed(SelectType.NOW, 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that selectNow() method doesn't block.",
        method = "selectNow",
        args = {}
    )
    public void test_selectNow_Timeout() throws IOException {
        selector.selectNow();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies select() method for Selector registered with SelectionKeys.OP_ACCEPT,  SelectionKeys.OP_CONNECT, SelectionKeys.OP_READ, SelectionKeys.OP_WRITE keys.",
        method = "select",
        args = {}
    )
    public void test_select() throws IOException {
        assert_select_OP_ACCEPT(SelectType.NULL, 0);
        assert_select_OP_CONNECT(SelectType.NULL, 0);
        assert_select_OP_READ(SelectType.NULL, 0);
        assert_select_OP_WRITE(SelectType.NULL, 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that ClosedSelectorException is thrown if select() method is called for closed Selector.",
        method = "select",
        args = {}
    )
    public void test_select_SelectorClosed() throws IOException {
        assert_select_SelectorClosed(SelectType.NULL, 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies select(long) method for Selector registered with SelectionKeys.OP_ACCEPT,  SelectionKeys.OP_CONNECT, SelectionKeys.OP_READ, SelectionKeys.OP_WRITE keys and different timeout's values.",
        method = "select",
        args = {long.class}
    )
    public void test_selectJ() throws IOException {
        assert_select_OP_ACCEPT(SelectType.TIMEOUT, 0);
        assert_select_OP_CONNECT(SelectType.TIMEOUT, 0);
        assert_select_OP_READ(SelectType.TIMEOUT, 0);
        assert_select_OP_WRITE(SelectType.TIMEOUT, 0);
        assert_select_OP_ACCEPT(SelectType.TIMEOUT, WAIT_TIME);
        assert_select_OP_CONNECT(SelectType.TIMEOUT, WAIT_TIME);
        assert_select_OP_READ(SelectType.TIMEOUT, WAIT_TIME);
        assert_select_OP_WRITE(SelectType.TIMEOUT, WAIT_TIME);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that ClosedSelectorException is thrown if select(long) method is called for closed Selector.",
        method = "select",
        args = {long.class}
    )
    public void test_selectJ_SelectorClosed() throws IOException {
        assert_select_SelectorClosed(SelectType.TIMEOUT, 0);
        selector = Selector.open();
        assert_select_SelectorClosed(SelectType.TIMEOUT, WAIT_TIME);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "select",
        args = {long.class}
    )
    public void test_selectJ_Exception() throws IOException {
        try {
            selector.select(-1);
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that select(timeout) doesn't block.",
        method = "select",
        args = {long.class}
    )
    public void test_selectJ_Timeout() throws IOException {
        selector.select(WAIT_TIME);
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies select(long) method for empty selection keys.",
            method = "select",
            args = {long.class}
    )
    public void test_selectJ_Empty_Keys() throws IOException {
        final long SELECT_TIMEOUT = 2000;
        long time1 = System.currentTimeMillis();
        selector.select(SELECT_TIMEOUT);
        long time2 = System.currentTimeMillis();
        assertEquals("elapsed time", SELECT_TIMEOUT, (time2 - time1),
                SELECT_TIMEOUT * 0.05); 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "wakeup",
        args = {}
    )
    public void test_wakeup() throws IOException {
        selector.wakeup();
        selectOnce(SelectType.NULL, 0);
        selector.wakeup();
        selectOnce(SelectType.TIMEOUT, 0);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                }
                selector.wakeup();
            }
        }.start();
        selectOnce(SelectType.NULL, 0);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                }
                selector.wakeup();
            }
        }.start();
        selectOnce(SelectType.TIMEOUT, 0);
    }
    public void test_keySetViewsModifications() throws IOException {
        Set<SelectionKey> keys = selector.keys();
        SelectionKey key1 = ssc.register(selector, SelectionKey.OP_ACCEPT);
        assertTrue(keys.contains(key1));
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey key2 = sc.register(selector, SelectionKey.OP_READ);
        assertTrue(keys.contains(key1));
        assertTrue(keys.contains(key2));
        key1.cancel();
        assertTrue(keys.contains(key1));
        selector.selectNow();
        assertFalse(keys.contains(key1));
        assertTrue(keys.contains(key2));
     }
    public void test_cancelledKeys() throws Exception {
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        final AtomicBoolean complete = new AtomicBoolean();
        final Pipe pipe = Pipe.open();
        pipe.source().configureBlocking(false);
        final SelectionKey key = pipe.source().register(selector, SelectionKey.OP_READ);
        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(500);
                    key.cancel();
                    assertFalse(key.isValid());
                    pipe.sink().write(ByteBuffer.allocate(4)); 
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    complete.set(true);
                }
            }
        };
        assertTrue(key.isValid());
        thread.start();
        do {
            assertEquals(0, selector.select(5000)); 
            assertEquals(0, selector.selectedKeys().size());
        } while (!complete.get()); 
        assertFalse(key.isValid());
        thread.join();
        assertNull(failure.get());
    }
    private void assert_select_SelectorClosed(SelectType type, int timeout)
            throws IOException {
        selector.close();
        try {
            selectOnce(type, timeout);
            fail("should throw ClosedSelectorException");
        } catch (ClosedSelectorException e) {
        }
    }
    private void assert_select_OP_ACCEPT(SelectType type, int timeout)
            throws IOException, ClosedChannelException {
        SocketChannel sc = SocketChannel.open();
        SocketChannel client = null;
        try {
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            sc.connect(LOCAL_ADDRESS);
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(ssc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_ACCEPT, key.readyOps());
            count = selectOnce(type, timeout);
            assertEquals(0,count);
            assertSame(selectedKeys, selector.selectedKeys());
            client = ssc.accept();
            selectedKeys.clear();
        } finally {
            try {
                sc.close();
            } catch (IOException e) {
            }
            if (null != client) {
                client.close();
            }
        }
        ssc.keyFor(selector).cancel();
    }
    private void assert_select_OP_CONNECT(SelectType type, int timeout)
            throws IOException, ClosedChannelException {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_CONNECT);
        try {
            sc.connect(LOCAL_ADDRESS);
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_CONNECT, key.readyOps());
            count = selectOnce(type, timeout);
            assertEquals(0, count);
            assertSame(selectedKeys, selector.selectedKeys());
            sc.finishConnect();            
            selectedKeys.clear();
        } finally {
            try {
                ssc.accept().close();
            } catch (Exception e) {
            }
            try {
                sc.close();
            } catch (IOException e) {
            }
        }
    }
    private void assert_select_OP_READ(SelectType type, int timeout)
            throws IOException {
        SocketChannel sc = SocketChannel.open();
        SocketChannel client = null;
        SocketChannel sc2 = SocketChannel.open();
        SocketChannel client2 = null;
        try {
            ssc.configureBlocking(true);
            sc.connect(LOCAL_ADDRESS);
            client = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
            client.configureBlocking(true);
            sc2.connect(LOCAL_ADDRESS);
            client2 = ssc.accept();
            sc2.configureBlocking(false);
            sc2.register(selector, SelectionKey.OP_READ);
            client2.configureBlocking(true);
            client.write(ByteBuffer.wrap("a".getBytes()));
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_READ, key.readyOps());
            count = selectOnce(type, timeout);
            assertEquals(0, count);
            assertSame(selectedKeys, selector.selectedKeys());
            sc.read(ByteBuffer.allocate(8));
            client2.write(ByteBuffer.wrap("a".getBytes()));
            count = blockingSelect(type, timeout);
            assertEquals(1, count);
            selectedKeys = selector.selectedKeys();
            assertEquals(2, selectedKeys.size());
        } finally {
            if (null != client) {
                try {
                    client.close();
                } catch (Exception e) {
                }
            }
            if (null != client2) {
                try {
                    client2.close();
                } catch (Exception e) {
                }
            }
            try {
                sc.close();
            } catch (Exception e) {
            }
            try {
                sc2.close();
            } catch (Exception e) {
            }
            ssc.configureBlocking(false);
        }
    }
    private void assert_select_OP_WRITE(SelectType type, int timeout)
            throws IOException {
        SocketChannel sc = SocketChannel.open();
        SocketChannel client = null;
        try {
            sc.connect(LOCAL_ADDRESS);
            ssc.configureBlocking(true);
            client = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_WRITE);
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_WRITE, key.readyOps());
            count = selectOnce(type, timeout);
            assertEquals(0, count);
            assertSame(selectedKeys, selector.selectedKeys());
        } finally {
            if (null != client) {
                client.close();
            }
            try {
                sc.close();
            } catch (IOException e) {
            }
            ssc.configureBlocking(false);
        }
    }
    private int blockingSelect(SelectType type, int timeout) throws IOException {
        int ret = 0;
        do {
            ret = selectOnce(type, timeout);
            if (ret > 0) {
                return ret;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        } while (true);
    }
    private int selectOnce(SelectType type, int timeout) throws IOException {
        int ret = 0;
        switch (type) {
        case NULL:
            ret = selector.select();
            break;
        case TIMEOUT:
            ret = selector.select(timeout);
            break;
        case NOW:
            ret = selector.selectNow();
            break;
        }
        return ret;
    }
}
