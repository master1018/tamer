@TestTargetClass(SocketChannel.class)
public class SocketChannelTest extends TestCase {
    private static final int CAPACITY_NORMAL = 200;
    private static final int CAPACITY_HUGE = 512 * 1024;
    private InetSocketAddress localAddr1;
    private InetSocketAddress localAddr2;
    private SocketChannel channel1;
    private SocketChannel channel2;
    private ServerSocket server1;
    private ServerSocket server2;
    private final static int TIMEOUT = 60000;
    private final static int EOF = -1;
    protected void setUp() throws Exception {
        super.setUp();
        this.localAddr1 = new InetSocketAddress("127.0.0.1",
                Support_PortManager.getNextPort());
        this.localAddr2 = new InetSocketAddress("127.0.0.1",
                Support_PortManager.getNextPort());
        this.channel1 = SocketChannel.open();
        this.channel2 = SocketChannel.open();
        this.server1 = new ServerSocket(localAddr1.getPort());
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        if (null != this.channel1) {
            try {
                this.channel1.close();
            } catch (Exception e) {
            }
        }
        if (null != this.channel2) {
            try {
                this.channel2.close();
            } catch (Exception e) {
            }
        }
        if (null != this.server1) {
            try {
                this.server1.close();
            } catch (Exception e) {
            }
        }
        if (null != this.server2) {
            try {
                this.server2.close();
            } catch (Exception e) {
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "SocketChannel",
            args = {SelectorProvider.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "open",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            clazz = SelectorProvider.class,
            method = "openSocketChannel",
            args = {}
        )
    })
    public void testConstructor() throws IOException {
        SocketChannel channel = 
                SelectorProvider.provider().openSocketChannel();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(), channel.provider());
        channel = SocketChannel.open();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(), channel.provider());
        MockSocketChannel chan = new MockSocketChannel(
                SelectorProvider.provider());
        assertTrue(chan.isConstructorCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "validOps",
        args = {}
    )
    public void testValidOps() {
        MockSocketChannel testMSChannel = new MockSocketChannel(null);
        assertEquals(13, this.channel1.validOps());
        assertEquals(13, testMSChannel.validOps());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "open",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "SocketChannel",
            args = {SelectorProvider.class}
        )
    })
    public void testOpen() throws IOException {
        java.nio.ByteBuffer[] buf = new java.nio.ByteBuffer[1];
        buf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        MockSocketChannel testMSChannel = new MockSocketChannel(null);
        MockSocketChannel testMSChannelnotnull = new MockSocketChannel(
                SelectorProvider.provider());
        SocketChannel testSChannel = MockSocketChannel.open();
        assertTrue(testSChannel.isOpen());
        assertNull(testMSChannel.provider());
        assertNotNull(testSChannel.provider());
        assertEquals(SelectorProvider.provider(), testSChannel.provider());
        assertNotNull(testMSChannelnotnull.provider());
        assertEquals(this.channel1.provider(), testMSChannelnotnull.provider());
        try {
            this.channel1.write(buf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isOpen",
        args = {}
    )
    public void testIsOpen() throws Exception {
        assertTrue(this.channel1.isOpen());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isConnected",
        args = {}
    )
    public void testIsConnected() throws Exception {
        assertFalse(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(tryFinish());
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        assertFalse(this.channel1.isConnected());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isConnectionPending",
        args = {}
    )
    public void testIsConnectionPending() throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.isConnectionPending());
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        assertFalse(this.channel1.isConnectionPending());
        assertFalse(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnectionPending());
        this.channel1.close();
        assertFalse(this.channel1.isConnectionPending());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of SocketChannel.",
            method = "validOps",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of SocketChannel.",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of SocketChannel.",
            method = "isRegistered",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of SocketChannel.",
            method = "isBlocking",
            args = {}
        )
    })
    public void testChannelBasicStatus() {
        Socket gotSocket = this.channel1.socket();
        assertFalse(gotSocket.isClosed());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isRegistered());
        assertEquals((SelectionKey.OP_CONNECT | SelectionKey.OP_READ |
                SelectionKey.OP_WRITE), this.channel1.validOps());
        assertEquals(SelectorProvider.provider(), this.channel1.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "open",
        args = {java.net.SocketAddress.class}
    )
    public void testOpenSocketAddress() throws IOException {
        this.channel1 = SocketChannel.open(localAddr1);
        assertTrue(this.channel1.isConnected());
        SecurityManager smngr = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager("blargh"));
        try {
            this.channel1 = SocketChannel.open(localAddr2);
            fail("Should throw SecurityException");
        } catch (SecurityException e) {
        }
        System.setSecurityManager(smngr);
        SocketAddress newTypeAddress = new SubSocketAddress();
        try {
            this.channel1 = SocketChannel.open(newTypeAddress);
            fail("Should throw UnexpectedAddressTypeException");
        } catch (UnsupportedAddressTypeException e) {
        }
        SocketAddress unresolvedAddress = 
                InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        try {
            this.channel1 = SocketChannel.open(unresolvedAddress);
            fail("Should throw UnresolvedAddressException");
        } catch (UnresolvedAddressException e) {
        }
        SocketChannel channel1IP = null;
        try {
            channel1IP = SocketChannel.open(null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertNull(channel1IP);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testReadByteBufferArray() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        ServerSocket testServer = new ServerSocket(Support_PortManager
                .getNextPort());
        try {
            try {
                this.channel1.read(byteBuf);
                fail("Should throw NPE");
            } catch (NullPointerException e) {
            }
            byteBuf = new java.nio.ByteBuffer[CAPACITY_NORMAL];
            try {
                this.channel1.read(byteBuf);
                fail("Should throw NotYetConnectedException");
            } catch (NotYetConnectedException e) {
            }
            long readNum = CAPACITY_NORMAL;
            readNum = testMSChannel.read(byteBuf);
            assertEquals(0, readNum);
            readNum = CAPACITY_NORMAL;
            readNum = testMSChannelnull.read(byteBuf);
            assertEquals(0, readNum);
        } finally {
            testServer.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testReadByteBufferArray_BufNull() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        try {
            this.channel1.read(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            testMSChannel.read(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            testMSChannelnull.read(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify AsynchronousCloseException," +
                "ClosedByInterruptException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        byteBuf = new java.nio.ByteBuffer[1];
        byteBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        testMSChannel.write(byteBuf);
        testMSChannelnull.write(byteBuf);
        this.channel1.close();
        try {
            this.channel1.write(byteBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_BufNull() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        this.channel1.connect(localAddr1);
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        byteBuf = new java.nio.ByteBuffer[1];
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    @AndroidOnly("Fails on RI. See comment below")
    public void testSocket_BasicStatusBeforeConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        Socket s1 = this.channel1.socket();
        assertSocketBeforeConnect(s1);
        Socket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_Block_BasicStatusAfterConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnected());
        Socket s1 = this.channel1.socket();
        assertSocketAfterConnect(s1, localAddr1);
        Socket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    @AndroidOnly("Fails on RI. See comment below")
    public void testSocket_NonBlock_BasicStatusAfterConnect() throws Exception {
        assertFalse(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        Socket s1 = this.channel1.socket();
        assertSocketBeforeConnect(s1);
        Socket s2 = this.channel1.socket();
        assertSame(s1, s2);
        assertTrue(tryFinish());
        assertTrue(this.channel1.isConnected());
        s1 = this.channel1.socket();
        assertSocketAfterConnect(s1, localAddr1);
        s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_Block_ActionsBeforeConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        Socket s = this.channel1.socket();
        assertSocketAction_Block_BeforeConnect(s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_Block_ActionsAfterConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnected());
        Socket s = this.channel1.socket();
        assertSocketAction_Block_AfterConnect(s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_NonBlock_ActionsAfterConnectBeforeFinish()
            throws IOException {
        assertFalse(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        Socket s1 = this.channel1.socket();
        assertSocketAction_NonBlock_BeforeConnect(s1);
        Socket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_NonBlock_ActionsAfterConnectAfterFinish()
            throws Exception {
        assertFalse(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        assertTrue(tryFinish());
        Socket s1 = this.channel1.socket();
        assertSocketAction_NonBlock_AfterConnect(s1);
        Socket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    private void assertSocketBeforeConnect(Socket s) throws IOException {
        assertFalse(s.isBound());
        assertFalse(s.isClosed());
        assertFalse(s.isConnected());
        assertFalse(s.getKeepAlive());
        try {
            s.getInputStream();
            fail("Should throw SocketException.");
        } catch (SocketException e) {
        }
        assertFalse(s.getOOBInline());
        try {
            s.getOutputStream();
            fail("Should throw SocketException.");
        } catch (SocketException e) {
        }
        assertEquals(-1, s.getSoLinger());
        assertFalse(s.getTcpNoDelay());
        assertFalse(s.isInputShutdown());
        assertFalse(s.isOutputShutdown());
        assertNull(s.getInetAddress());
        assertEquals(s.getLocalAddress().getHostAddress(), "0.0.0.0");
        assertFalse(s.getReuseAddress());
        assertNull(s.getLocalSocketAddress());
        assertEquals(0, s.getPort());
        assertTrue(s.getReceiveBufferSize() >= 8192);
        assertNull(s.getRemoteSocketAddress());
        assertTrue(s.getSendBufferSize() >= 8192);
        assertEquals(0, s.getSoTimeout());
        assertEquals(0, s.getTrafficClass());
        assertEquals(-1, s.getLocalPort());
    }
    private void assertSocketAfterConnect(Socket s, InetSocketAddress address)
            throws IOException {
        assertTrue(s.isBound());
        assertFalse(s.isClosed());
        assertTrue(s.isConnected());
        assertFalse(s.getKeepAlive());
        assertNotNull(s.getInputStream());
        assertNotNull(s.getOutputStream());
        assertFalse(s.getOOBInline());
        assertEquals(-1, s.getSoLinger());
        assertFalse(s.getTcpNoDelay());
        assertFalse(s.isInputShutdown());
        assertFalse(s.isOutputShutdown());
        assertSame(s.getInetAddress(), address.getAddress());
        assertEquals(this.localAddr1.getAddress(), s.getLocalAddress());
        assertEquals(address.getPort(), s.getPort());
        assertNotNull(s.getLocalSocketAddress());
        assertTrue(s.getReceiveBufferSize() >= 8192);
        assertNotSame(address, s.getRemoteSocketAddress());
        assertEquals(address, s.getRemoteSocketAddress());
        assertTrue(s.getSendBufferSize() >= 8192);
        assertEquals(0, s.getSoTimeout());
        assertEquals(0, s.getTrafficClass());
    }
    private void assertSocketAction_Block_BeforeConnect(Socket s)
            throws IOException {
        assertFalse(this.channel1.isConnected());
        this.server2 = new ServerSocket(localAddr2.getPort());
        s.connect(localAddr2);
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());
        assertSocketAfterConnect(s, localAddr2);
        try {
            s.bind(localAddr2);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }
    private void assertSocketAction_NonBlock_BeforeConnect(Socket s)
            throws IOException {
        assertFalse(this.channel1.isConnected());
        this.server2 = new ServerSocket(localAddr2.getPort());
        try {
            s.connect(localAddr2);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e1) {
        }
        if (this.channel1.isConnectionPending()) {
            try {
                s.bind(localAddr2);
                fail("Should throw ConnectionPendingException");
            } catch (ConnectionPendingException e1) {
            }
        } else {
            try {
                s.bind(localAddr2);
                fail("Should throw BindException");
            } catch (BindException e1) {
            }
        }
        assertFalse(this.channel1.isConnected());
        assertFalse(s.isConnected());
        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }
    private void assertSocketAction_Block_AfterConnect(Socket s)
            throws IOException {
        assertEquals(localAddr1.getPort(), s.getPort());
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());
        try {
            s.connect(localAddr2);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        try {
            s.bind(localAddr2);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }
    private void assertSocketAction_NonBlock_AfterConnect(Socket s)
            throws IOException {
        assertEquals(localAddr1.getPort(), s.getPort());
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());
        if (this.channel1.isConnectionPending()) {
            try {
                s.connect(localAddr2);
                fail("Should throw AlreadyConnectedException");
            } catch (AlreadyConnectedException e) {
            }
        } else {
            try {
                s.connect(localAddr2);
                fail("Should throw IllegalBlockingModeException");
            } catch (IllegalBlockingModeException e) {
            }
        }
        try {
            s.bind(localAddr2);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_Norml_NoServer_Block() throws Exception {
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_Norml_NoServer_NonBlock() throws Exception {
        connectNoServerNonBlock();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_Norml_Server_Block() throws Exception {
        connectServerBlock();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_Norml_Server_NonBlock() throws Exception {
        connectServerNonBlock();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_ServerClosed_Block() throws Exception {
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();
        ensureServerClosed();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_ServerClosed_NonBlock() throws Exception {
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        ensureServerClosed();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_ServerClosedAfterFinish_Block() throws Exception {
        connectServerBlock();
        ensureServerClosed();
        assertTrue(this.channel1.isOpen());
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_ServerClosedAfterFinish_NonBlock() throws Exception {
        connectServerNonBlock();
        ensureServerClosed();
        assertTrue(this.channel1.isOpen());
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_ServerStartLater_Block() throws Exception {
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
        }
        statusChannelClosed();
        ensureServerOpen();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_ServerStartLater_NonBlock() throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        ensureServerOpen();
        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            this.channel1.close();
        } catch (ConnectException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_FinishTwice_NoServer_NonBlock() throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            assertFalse(this.channel1.finishConnect());
        } catch (ConnectException e) {
        }
        statusChannelClosed();
        try {
            assertFalse(this.channel1.finishConnect());
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_FinishTwice_Server_Block() throws Exception {
        connectServerBlock();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_FinishTwice_Server_NonBlock() throws Exception {
        connectServerNonBlock();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case:  connect-->finish-->connect-->close. Verifies ClosedChannelException, ConnectException.",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case:  connect-->finish-->connect-->close. Verifies ClosedChannelException, ConnectException.",
            method = "finishConnect",
            args = {}
        )
    })   
    public void testCFII_ConnectAfterFinish_NoServer_Block() throws Exception {
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case: connect-->finish-->connect-->close. Verifies ConnectionPendingException, ConnectException.",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case: connect-->finish-->connect-->close. Verifies ConnectionPendingException, ConnectException.",
            method = "finishConnect",
            args = {}
        )
    })    
    public void testCFII_ConnectAfterFinish_NoServer_NonBlock()
            throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        ensureServerClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies AlreadyConnectedException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )        
    public void testCFII_ConnectAfterFinish_Server_Block() throws Exception {
        connectServerBlock();
        if (!this.channel1.isConnected()) {
            fail("Connection failed," +
                    "testCFII_ConnectAfterFinish_Server_Block not finished.");
        }
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        ensureServerClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies AlreadyConnectedException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )    
    public void testCFII_ConnectAfterFinish_Server_NonBlock() throws Exception {
        connectServerNonBlock();
        if (!this.channel1.isConnected()) {
            fail("Connection failed," +
                    "testCFII_ConnectAfterFinish_Server_Block not finished.");
        }
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException or a ConnectionPendingException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        ensureServerClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case: connect-->connect-->finish-->close. Verifies ConnectionPendingException, ConnectException.",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case: connect-->connect-->finish-->close. Verifies ConnectionPendingException, ConnectException.",
            method = "finishConnect",
            args = {}
        )
    }) 
    public void testCFII_ConnectTwice_NoServer_NonBlock() throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        ensureServerClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case connect-->connect-->finish-->close. Verifies AlreadyConnectedException.",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case connect-->connect-->finish-->close. Verifies AlreadyConnectedException.",
            method = "finishConnect",
            args = {}
        )
    }) 
    public void testCFII_ConnectTwice_Server_Block() throws Exception {
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        ensureServerClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
        }
        statusConnected_NotPending();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case connect-->connect-->finish-->close. Verifies ConnectionPendingException.",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies case connect-->connect-->finish-->close. Verifies ConnectionPendingException.",
            method = "finishConnect",
            args = {}
        )
    }) 
    public void testCFII_ConnectTwice_Server_NonBlock() throws Exception {
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        ensureServerClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
        }
        statusNotConnected_Pending();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_FinishFirst_NoServer_Block() throws Exception {
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        statusNotConnected_NotPending();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_FinishFirst_NoServer_NonBlock() throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })    
    public void testCFII_FinishFirst_Server_Block() throws Exception {
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        statusNotConnected_NotPending();
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })    
    public void testCFII_FinishFirst_Server_NonBlock() throws Exception {
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testCFII_Null() throws Exception {
        statusNotConnected_NotPending();
        try {
            this.channel1.connect(null);
            fail("Should throw an IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testCFII_UnsupportedType() throws Exception {
        statusNotConnected_NotPending();
        SocketAddress newTypeAddress = new SubSocketAddress();
        try {
            this.channel1.connect(newTypeAddress);
            fail("Should throw an UnsupportedAddressTypeException here.");
        } catch (UnsupportedAddressTypeException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testCFII_Unresolved() throws IOException {
        statusNotConnected_NotPending();
        InetSocketAddress unresolved = new InetSocketAddress(
                "unresolved address", 1080);
        try {
            this.channel1.connect(unresolved);
            fail("Should throw an UnresolvedAddressException here.");
        } catch (UnresolvedAddressException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testCFII_EmptyHost() throws Exception {
        statusNotConnected_NotPending();
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        try {
            this.channel1.connect(new InetSocketAddress("", port));
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies ClosedChannelException.",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies ClosedChannelException.",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_CloseFirst() throws Exception {
        this.channel1.close();
        statusChannelClosed();
        ensureServerOpen();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
        try {
            this.channel1.configureBlocking(false);
            fail("Should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
        }
        statusChannelClosed();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    })
    public void testCFII_StatusAfterFinish() throws Exception {
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        try {
            channel1.connect(localAddr1);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
        }
        assertFalse(this.channel1.isOpen());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnectionPending());
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        try {
            assertFalse(this.channel1.finishConnect());
            fail("Should throw IOException: Connection refused");
        } catch (ConnectException e) {
        }
        statusChannelClosed();
        ensureServerOpen();
        this.channel1 = SocketChannel.open();
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.finishConnect());
        statusConnected_NotPending();
        this.channel1.close();
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        tryFinish();
        this.channel1.close();
    }
    private void ensureServerClosed() throws IOException {
        if (null != this.server1) {
            this.server1.close();
            assertTrue(this.server1.isClosed());
        }
        if (null != this.server2) {
            this.server2.close();
            assertTrue(this.server2.isClosed());
        }
    }
    private void ensureServerOpen() throws IOException {
        ensureServerClosed();
        this.server1 = new ServerSocket(localAddr1.getPort());
        this.server2 = new ServerSocket(localAddr2.getPort());
        assertTrue(this.server1.isBound());
        assertTrue(this.server2.isBound());
    }
    private void connectNoServerNonBlock() throws Exception {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            assertFalse(this.channel1.finishConnect());
        } catch (ConnectException e) {
        }
        ensureServerClosed();
    }
    private void connectServerNonBlock() throws Exception {
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        tryFinish();
    }
    private void connectServerBlock() throws Exception {
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();
        tryFinish();
    }
    private void statusChannelClosed() {
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertFalse(this.channel1.isOpen());
    }
    private void statusNotConnected_NotPending() {
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
    }
    private void statusNotConnected_Pending() {
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
    }
    private void statusConnected_NotPending() {
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
    }
    private boolean tryFinish() throws IOException {
        boolean connected = false;
        assertTrue(this.channel1.isOpen());
        try {
            connected = this.channel1.finishConnect();
        } catch (SocketException e) {
        }
        if (connected) {
            statusConnected_NotPending();
        }
        return connected;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testCFII_Data_ConnectWithServer() throws Exception {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBufArr, 0, 1));
        this.channel1.configureBlocking(false);
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        )
    }) 
    public void testCFII_Data_ConnectWithServer_nonBlocking() throws Exception {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        this.channel1.configureBlocking(false);
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertTrue(tryFinish());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        assertEquals(CAPACITY_NORMAL, this.channel1
                .write(writeBufArr, 0, 1));
        this.channel1.configureBlocking(false);
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "finishConnect",
        args = {}
    )
    public void testCFII_Data_FinishConnect_nonBlocking() throws IOException {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        this.channel1.configureBlocking(false);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        this.server1.accept();
        assertTrue(tryFinish());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        assertEquals(CAPACITY_NORMAL, this.channel1
                .write(writeBufArr, 0, 1));
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "finishConnect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies IOException",
            method = "open",
            args = {SocketAddress.class}
        )
    }) 
    public void testCFII_Data_FinishConnect_AddrSetServerStartLater()
            throws IOException {
        ensureServerClosed();
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        this.channel1.configureBlocking(false);
        try {
            SocketChannel.open(localAddr1);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
        }
        assertTrue(this.channel1.isOpen());
        assertFalse(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnectionPending());
        this.channel1.configureBlocking(true);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
        }
        assertTrue(this.channel1.isBlocking());
        try {
            this.channel1.finishConnect();
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isConnected());
        ensureServerOpen();
            assertFalse(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ConnectionPendingException");
        } catch (ConnectionPendingException e) {
        }
        this.channel1.configureBlocking(true);
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ConnectionPendingException");
        } catch (ConnectionPendingException e) {
        }
        tryFinish();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "finishConnect",
        args = {}
    ) 
    public void testCFII_Data_FinishConnect_ServerStartLater()
            throws IOException {
        ensureServerClosed();
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        this.channel1.configureBlocking(true);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
        }
        try {
            this.channel1.finishConnect();
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isConnected());
        ensureServerOpen();
        assertFalse(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ConnectionPendingException");
        } catch (ConnectionPendingException e) {
        }
        this.channel1.configureBlocking(true);
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ConnectionPendingException");
        } catch (ConnectionPendingException e) {
        }
        tryFinish();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "finishConnect",
        args = {}
    )
    public void testCFII_Data_FinishConnect_Blocking() throws IOException {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        this.channel1.configureBlocking(true);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertTrue(tryFinish());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        assertEquals(CAPACITY_NORMAL, this.channel1
                .write(writeBufArr, 0, 1));
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
        }
        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "finishConnect",
        args = {}
    )  
    public void test_finishConnect() throws Exception {
        SocketAddress address = new InetSocketAddress("localhost", 2046);
        ServerSocketChannel theServerChannel = ServerSocketChannel.open();
        ServerSocket serversocket = theServerChannel.socket();
        serversocket.setReuseAddress(true);
        serversocket.bind(address);
        boolean doneNonBlockingConnect = false;
        while (!doneNonBlockingConnect) {
            channel1 = SocketChannel.open();
            channel1.configureBlocking(false);
            boolean connected = channel1.connect(address);
            if (!connected) {
                channel1.configureBlocking(true);
                doneNonBlockingConnect = channel1.finishConnect();
            } else {
                fail("Non blocking connect was connected too fast." +
                        "Could not test finishConnect().");
            }
            if (doneNonBlockingConnect) {
                tryFinish();
            } else {
                fail("finishConnect() did not finish the connection.");
            }
            channel1.close();
        }
        if (!serversocket.isClosed()) {
            serversocket.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    ) 
    public void test_readLByteBuffer_Blocking() throws IOException {
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        acceptedSocket.close();
        ByteBuffer readContent = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        int totalCount = 0;
        int count = 0;
        long startTime = System.currentTimeMillis();
        while (totalCount <= CAPACITY_NORMAL) {
            count = channel1.read(readContent);
            if (EOF == count) {
                break;
            }
            totalCount += count;
            assertTimeout(startTime, TIMEOUT);
        }
        assertEquals(CAPACITY_NORMAL, totalCount);
        assertEquals(CAPACITY_NORMAL, readContent.position());
        readContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContent.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    ) 
    public void test_readLByteBuffer_Nonblocking() throws IOException {
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        acceptedSocket.close();
        channel1.configureBlocking(false);
        ByteBuffer readContent = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        int totalCount = 0;
        int count = 0;
        long startTime = System.currentTimeMillis();
        while (totalCount <= CAPACITY_NORMAL) {
            count = channel1.read(readContent);
            if (EOF == count) {
                break;
            }
            totalCount += count;
            assertTimeout(startTime, TIMEOUT);
        }
        assertEquals(CAPACITY_NORMAL, totalCount);
        assertEquals(CAPACITY_NORMAL, readContent.position());
        readContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContent.get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLjava_nio_ByteBuffer_Blocking() throws IOException {
        ByteBuffer writeContent = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent.put((byte) i);
        }
        writeContent.flip();
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        int writtenCount = channel1.write(writeContent);
        assertEquals(CAPACITY_NORMAL, writtenCount);
        assertEquals(CAPACITY_NORMAL, writeContent.position());
        channel1.close();
        InputStream in = acceptedSocket.getInputStream();
        int totalCount = 0;
        int count = 0;
        byte[] readContent = new byte[CAPACITY_NORMAL + 1];
        acceptedSocket.setSoTimeout(TIMEOUT);
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }
        assertEquals(CAPACITY_NORMAL, totalCount);
        writeContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent.get(), readContent[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_writeLjava_nio_ByteBuffer_NonBlocking() throws Exception {
        ByteBuffer writeContent = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent.put((byte) i);
        }
        writeContent.flip();
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        channel1.configureBlocking(false);
        int writtenTotalCount = 0;
        int writtenCount = 0;
        long startTime = System.currentTimeMillis();
        while (writtenTotalCount < CAPACITY_NORMAL) {
            writtenCount = channel1.write(writeContent);
            writtenTotalCount += writtenCount;
            assertTimeout(startTime, TIMEOUT);
        }
        assertEquals(CAPACITY_NORMAL, writtenTotalCount);
        assertEquals(CAPACITY_NORMAL, writeContent.position());
        channel1.close();
        InputStream in = acceptedSocket.getInputStream();
        byte[] readContent = new byte[CAPACITY_NORMAL + 1];
        int totalCount = 0;
        int count = 0;
        acceptedSocket.setSoTimeout(TIMEOUT);
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }
        assertEquals(CAPACITY_NORMAL, totalCount);
        writeContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent.get(), readContent[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    @BrokenTest("Occasionally fail in CTS, but works in CoreTestRunner")
    public void test_writeLjava_nio_ByteBuffer_Nonblocking_HugeData() throws IOException {
        ByteBuffer writeContent = ByteBuffer.allocate(CAPACITY_HUGE);
        for (int i = 0; i < CAPACITY_HUGE; i++) {
            writeContent.put((byte) i);
        }
        writeContent.flip();
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        channel1.configureBlocking(false);
        int writtenTotalCount = 0;
        int writtenCount = 1;
        long startTime = System.currentTimeMillis();
        while (writtenTotalCount < CAPACITY_HUGE && writtenCount > 0) {
            writtenCount = channel1.write(writeContent);
            if (writtenCount == 0 && writtenTotalCount < CAPACITY_HUGE) {
                assertEquals(0, channel1.write(writeContent));
                break;
            }
            writtenTotalCount += writtenCount;
            assertTimeout(startTime, TIMEOUT);
        }
    }
    private void assertTimeout(long startTime, long timeout) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - startTime) > timeout) {
            fail("Timeout");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer readBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnectionPending());
        assertFalse(this.channel1.isConnected());
        assertTrue(tryFinish());
        assertEquals(0, this.channel1.read(readBuf));
        this.channel1.close();
        try {
            channel1.read(readBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_Direct() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer readBuf = java.nio.ByteBuffer
                .allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnectionPending());
        assertFalse(this.channel1.isConnected());
        assertTrue(tryFinish());
        assertEquals(0, this.channel1.read(readBuf));
        this.channel1.close();
        try {
            channel1.read(readBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    public void testReadByteBuffer_Direct2() throws IOException {
        byte[] request = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteBuffer buffer = ByteBuffer.allocateDirect(128);
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0), 5);
        Socket client = new Socket(InetAddress.getLocalHost(), server.socket()
                .getLocalPort());
        client.setTcpNoDelay(false);
        Socket worker = server.socket().accept();
        SocketChannel workerChannel = worker.getChannel();
        OutputStream out = client.getOutputStream();
        out.write(request);
        out.close();
        buffer.limit(5);
        int bytesRead = workerChannel.read(buffer);
        assertEquals(5, bytesRead);
        assertEquals(5, buffer.position());
        buffer.limit(request.length);
        bytesRead = workerChannel.read(buffer);
        assertEquals(6, bytesRead);
        buffer.flip();
        assertEquals(request.length, buffer.limit());
        assertEquals(ByteBuffer.wrap(request), buffer);
        client.close();
        worker.close();
        server.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_BufNull() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer readBuf = java.nio.ByteBuffer.allocate(0);
        this.channel1.configureBlocking(false);
        try {
            channel1.read((java.nio.ByteBuffer) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(tryFinish());
        try {
            this.channel1.read((java.nio.ByteBuffer) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        assertEquals(0, this.channel1.read(readBuf));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayIntInt() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[2];
        readBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        readBuf[1] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnectionPending());
        assertFalse(this.channel1.isConnected());
        assertTrue(tryFinish());
        assertEquals(0, this.channel1.read(readBuf, 0, 1));
        assertEquals(0, this.channel1.read(readBuf, 0, 2));
        this.channel1.close();
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )    
    public void testReadByteBufferArrayIntInt_Direct() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[2];
        readBuf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnectionPending());
        assertFalse(this.channel1.isConnected());
        assertTrue(tryFinish());
        assertEquals(0, this.channel1.read(readBuf, 0, 1));
        assertEquals(0, this.channel1.read(readBuf, 0, 2));
        this.channel1.close();
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayIntInt_BufNull() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[2];
        readBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        this.channel1.configureBlocking(false);
        try {
            channel1.read(null, 0, 0);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(tryFinish());
        try {
            channel1.read(null, 0, 0);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            channel1.read(readBuf, 0, 2);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        assertEquals(0, this.channel1.read(readBuf, 0, 1));
        this.channel1.close();
        try {
            channel1.read(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer() throws IOException {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify AsynchronousCloseException, ClosedByInterruptException.",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_Direct() throws IOException {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_BufNull() throws IOException {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer.allocate(0);
        this.channel1.connect(localAddr1);
        assertEquals(this.channel1.write(writeBuf), 0);
        try {
            this.channel1.write((java.nio.ByteBuffer) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify AsynchronousCloseException," +
                "ClosedByInterruptException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayIntInt() throws IOException {
        java.nio.ByteBuffer[] writeBuf = new java.nio.ByteBuffer[2];
        writeBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        writeBuf[1] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 1));
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 2));
        writeBuf[0].flip();
        writeBuf[1].flip();
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf, 0, 2));
        this.channel1.close();
        try {
            channel1.write(writeBuf, 0, 1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayIntInt_Direct() throws IOException {
        java.nio.ByteBuffer[] writeBuf = new java.nio.ByteBuffer[2];
        writeBuf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 1));
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 2));
        writeBuf[0].flip();
        writeBuf[1].flip();
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf, 0, 2));
        this.channel1.close();
        try {
            channel1.write(writeBuf, 0 ,1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayIntInt_BufNull() throws IOException {
        java.nio.ByteBuffer[] writeBuf = new java.nio.ByteBuffer[0];
        this.channel1.connect(localAddr1);
        try {
            this.channel1.write(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        assertEquals(0, this.channel1.write(writeBuf, 0, 0));
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        writeBuf = new java.nio.ByteBuffer[1];
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        writeBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        this.server1.close();
        try {
            channel1.read(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayIntInt_SizeError() throws IOException {
        java.nio.ByteBuffer[] writeBuf = 
                {java.nio.ByteBuffer.allocate(CAPACITY_NORMAL)};
        this.channel1.connect(localAddr1);
        try {
            this.channel1.write(writeBuf, -1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(0, this.channel1.write(writeBuf, 0, 0));
        try {
            this.channel1.write(writeBuf, 0, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayIntInt_SizeError() throws IOException {
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[0];
        this.channel1.connect(localAddr1);
        try {
            this.channel1.read(null, -1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(0, this.channel1.read(readBuf, 0, 0));
        try {
            this.channel1.read(readBuf, 0, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        readBuf = new java.nio.ByteBuffer[1];
        try {
            this.channel1.read(readBuf, 2, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.read(readBuf, 2, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        this.server1.close();
        try {
            assertEquals(CAPACITY_NORMAL, this.channel1.read(null, -1, -1));
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    public void test_read$LByteBuffer() throws IOException {
        MockSocketChannel sc = new MockSocketChannel(null);
        ByteBuffer [] byteBufferArray = { ByteBuffer.allocate(1), ByteBuffer.allocate(1)};
        sc.read(byteBufferArray);
        assertTrue(sc.isReadCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    ) 
    public void test_read$LByteBuffer_blocking() throws Exception {
        assert_read$LByteBuffer(true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    ) 
    public void test_read$LByteBuffer_nonblocking() throws Exception {
        assert_read$LByteBuffer(false);
    }
    private void assert_read$LByteBuffer(boolean isBlocking) throws IOException {
        byte[] writeContent = new byte[CAPACITY_NORMAL * 2];
        for (int i = 0; i < CAPACITY_NORMAL * 2; i++) {
            writeContent[i] = (byte) i;
        }
        ByteBuffer[] readContents = new ByteBuffer[2];
        readContents[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        readContents[1] = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        acceptedSocket.close();
        channel1.configureBlocking(isBlocking);
        long startTime = System.currentTimeMillis();
        long totalRead = 0;
        long countRead = 0;
        while (totalRead <= CAPACITY_NORMAL * 2) {
            countRead = channel1.read(readContents);
            if (0 == countRead && !readContents[1].hasRemaining()) {
                break;
            }
            if (EOF == countRead) {
                break;
            }
            totalRead += countRead;
            assertTimeout(startTime, TIMEOUT);
        }
        assertEquals(CAPACITY_NORMAL * 2, totalRead);
        assertEquals(CAPACITY_NORMAL, readContents[0].position());
        assertEquals(CAPACITY_NORMAL, readContents[1].position());
        readContents[0].flip();
        readContents[1].flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContents[0].get());
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            assertEquals(writeContent[i], readContents[1].get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    ) 
    public void test_read$LByteBufferII_blocking() throws Exception {
        assert_read$LByteBufferII(true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    ) 
    public void test_read$LByteBufferII_nonblocking() throws Exception {
        assert_read$LByteBufferII(false);
    }
    private void assert_read$LByteBufferII(boolean isBlocking) throws IOException {
        byte[] writeContent = new byte[CAPACITY_NORMAL * 2];
        for (int i = 0; i < CAPACITY_NORMAL * 2; i++) {
            writeContent[i] = (byte) i;
        }
        ByteBuffer[] readContents = new ByteBuffer[2];
        readContents[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        readContents[1] = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        acceptedSocket.close();
        channel1.configureBlocking(isBlocking);
        long startTime = System.currentTimeMillis();
        long totalRead = 0;
        long countRead = 0;
        while (totalRead <= CAPACITY_NORMAL * 2) {
            countRead = channel1.read(readContents, 0, 2);
            if (0 == countRead && !readContents[1].hasRemaining()) {
                break;
            }
            if (EOF == countRead) {
                break;
            }
            totalRead += countRead;
            assertTimeout(startTime, TIMEOUT);
        }
        assertEquals(CAPACITY_NORMAL * 2, totalRead);
        assertEquals(CAPACITY_NORMAL, readContents[0].position());
        assertEquals(CAPACITY_NORMAL, readContents[1].position());
        readContents[0].flip();
        readContents[1].flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContents[0].get());
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            assertEquals(writeContent[i], readContents[1].get());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write$LByteBuffer_blocking() throws Exception {
        assert_write$LByteBuffer(true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write$LByteBuffer_nonblocking() throws Exception {
        assert_write$LByteBuffer(false);
    }
    private void assert_write$LByteBuffer(boolean isBlocking)
            throws IOException {
        ByteBuffer writeContents[] = new ByteBuffer[2];
        writeContents[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        writeContents[1] = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContents[0].put((byte) i);
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            writeContents[1].put((byte) i);
        }
        writeContents[0].flip();
        writeContents[1].flip();
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        channel1.configureBlocking(isBlocking);
        assertEquals(2 * CAPACITY_NORMAL, channel1.write(writeContents));
        assertEquals(CAPACITY_NORMAL, writeContents[0].position());
        assertEquals(CAPACITY_NORMAL, writeContents[1].position());
        channel1.close();
        InputStream in = acceptedSocket.getInputStream();
        byte[] readContent = new byte[CAPACITY_NORMAL * 2 + 1];
        int totalCount = 0;
        int count = 0;
        acceptedSocket.setSoTimeout(TIMEOUT);
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }
        assertEquals(CAPACITY_NORMAL * 2, totalCount);
        writeContents[0].flip();
        writeContents[1].flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContents[0].get(), readContent[i]);
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            assertEquals(writeContents[1].get(), readContent[i]);
        }
    }
    public void test_write$LByteBuffer() throws IOException {
        MockSocketChannel sc = new MockSocketChannel(null);
        ByteBuffer [] byteBufferArray = { ByteBuffer.allocate(1), ByteBuffer.allocate(1)};
        sc.write(byteBufferArray);
        assertTrue(sc.isWriteCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_blocking() throws Exception {
        assert_write$LByteBufferII(true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_write$LByteBufferII_nonblocking() throws Exception {
        assert_write$LByteBufferII(false);
    }
    private void assert_write$LByteBufferII(boolean isBlocking)
            throws IOException {
        ByteBuffer writeContents[] = new ByteBuffer[2];
        writeContents[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        writeContents[1] = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContents[0].put((byte) i);
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            writeContents[1].put((byte) i);
        }
        writeContents[0].flip();
        writeContents[1].flip();
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        channel1.configureBlocking(isBlocking);
        assertEquals(CAPACITY_NORMAL, channel1.write(writeContents, 0, 1));
        assertEquals(CAPACITY_NORMAL, channel1.write(writeContents, 1, 1));
        assertEquals(CAPACITY_NORMAL, writeContents[0].position());
        assertEquals(CAPACITY_NORMAL, writeContents[1].position());
        channel1.close();
        InputStream in = acceptedSocket.getInputStream();
        byte[] readContent = new byte[CAPACITY_NORMAL * 2 + 1];
        int totalCount = 0;
        int count = 0;
        acceptedSocket.setSoTimeout(TIMEOUT);
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }
        assertEquals(CAPACITY_NORMAL * 2, totalCount);
        writeContents[0].flip();
        writeContents[1].flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContents[0].get(), readContent[i]);
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            assertEquals(writeContents[1].get(), readContent[i]);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "socket",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            clazz = SelectableChannel.class,
            method = "configureBlocking",
            args = {boolean.class}
        )
    })
    public void testSocket_configureblocking() throws IOException {
        byte[] serverWBuf = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < serverWBuf.length; i++) {
            serverWBuf[i] = (byte) i;
        }
        java.nio.ByteBuffer buf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL + 1);
        channel1.connect(localAddr1);
        server1.accept();
        Socket sock = this.channel1.socket();
        channel1.configureBlocking(false);
        assertFalse(channel1.isBlocking());
        OutputStream channelSocketOut = sock.getOutputStream();
        try {
            channelSocketOut.write(buf.array());
            fail("Non-Blocking mode should cause IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        channel1.configureBlocking(true);
        assertTrue(channel1.isBlocking());
        channelSocketOut.write(buf.array());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_socketChannel_read_ByteBufferII_remoteClosed()
            throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr2);
        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr2);
        ssc.accept().close();
        ByteBuffer[] buf = {ByteBuffer.allocate(10)};
        assertEquals(-1, sc.read(buf, 0, 1));
        ssc.close();
        sc.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_socketChannel_write_ByteBufferII() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr2);
        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr2);
        SocketChannel sock = ssc.accept();
        ByteBuffer[] buf = {ByteBuffer.allocate(10), null};
        try {
            sc.write(buf, 0, 2);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        ssc.close();
        sc.close();
        ByteBuffer target = ByteBuffer.allocate(10);
        assertEquals(-1, sock.read(target));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_socketChannel_read_ByteBufferII_bufNULL() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr2);
        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr2);
        ssc.accept();
        ByteBuffer[] buf = new ByteBuffer[2];
        buf[0] = ByteBuffer.allocate(1);
        try {
            sc.read(buf, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ssc.close();
        sc.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_socketChannel_write_close() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr2);
        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr2);
        SocketChannel sock = ssc.accept();
        ByteBuffer buf = null;
        ssc.close();
        sc.close();
        try {
            sc.write(buf);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        sock.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_socketChannel_write_ByteBuffer_posNotZero()
            throws Exception {
        final String testStr = "Hello World";
        ByteBuffer readBuf = ByteBuffer.allocate(11);
        ByteBuffer buf = ByteBuffer.wrap(testStr.getBytes());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr2);
        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr2);
        buf.position(2);
        ssc.accept().write(buf);
        assertEquals(9, sc.read(readBuf));
        buf.flip();
        readBuf.flip();
        byte[] read = new byte[9];
        byte[] write = new byte[11];
        buf.get(write);
        readBuf.get(read);
        for (int i = 0; i < 9; i++) {
            assertEquals(read[i], write[i + 2]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    ) 
    public void test_read$LByteBuffer_Blocking() throws IOException {
        byte[] data = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            data[i] = (byte) i;
        }
        ByteBuffer[] buf = new ByteBuffer[2];
        buf[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        buf[1] = ByteBuffer.allocate(CAPACITY_NORMAL);
        channel1.connect(localAddr1);
        Socket socket = null;
        try {
            socket = server1.accept();
            OutputStream out = socket.getOutputStream();
            out.write(data);
            channel1.read(buf);
        } finally {
            if (null != socket) {
                socket.close();
            } else {
                fail();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalBlockingModeException, NullPointerException.",
        method = "socket",
        args = {}
    )
    public void test_socket_getOutputStream_nonBlocking_read_Exception()
            throws IOException {
        channel1.connect(this.localAddr1);
        InputStream is = channel1.socket().getInputStream();
        channel1.configureBlocking(false);
        try {
            is.read();
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        try {
            is.read(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        is.close();
        try {
            is.read(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read();
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        try {
            is.read(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException, ClosedChannelException, IndexOutOfBoundsException.",
        method = "socket",
        args = {}
    )
    public void test_socket_getOutputStream_blocking_read_Exception()
            throws IOException {
        channel1.connect(this.localAddr1);
        InputStream is = channel1.socket().getInputStream();
        try {
            is.read(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        is.close();
        try {
            is.read(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            is.read(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            is.read(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException, IllegalBlockingModeException.",
        method = "socket",
        args = {}
    )
    public void test_socket_getOutputStream_nonBlocking_write_Exception()
            throws IOException {
        channel1.connect(this.localAddr1);
        OutputStream os = channel1.socket().getOutputStream();
        channel1.configureBlocking(false);
        try {
            os.write(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(1);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        try {
            os.write(1);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        try {
            os.write(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        os.close();
        try {
            os.write(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(1);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        try {
            os.write(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException, IndexOutOfBoundsException, ClosedChannelException.",
        method = "socket",
        args = {}
    )
    public void test_socket_getOutputStream_blocking_write_Exception()
            throws IOException {
        channel1.connect(this.localAddr1);
        OutputStream os = channel1.socket().getOutputStream();
        try {
            os.write(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        os.close();
        try {
            os.write(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            os.write(null, 1, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            os.write(null, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void test_socket_getOutputStream_write_oneByte()
            throws IOException {
        int MAGIC = 123;
        channel1.connect(this.localAddr1);
        OutputStream os = channel1.socket().getOutputStream();
        Socket acceptedSocket = server1.accept();
        InputStream in = acceptedSocket.getInputStream();
        os.write(MAGIC);
        channel1.close();
        int lastByte = in.read();
        if (lastByte == -1) {
            fail("Server received nothing. Expected 1 byte.");
        } else if (lastByte != MAGIC) {
            fail("Server received wrong single byte: " + lastByte
                    + ", expected: " + MAGIC);
        }
        lastByte = in.read();
        if (lastByte != -1) {
            fail("Server received too long sequence. Expected 1 byte.");
        }
    }
    public void testSocket_setOptions() throws IOException {
        channel1.connect(localAddr1);
        Socket socket = channel1.socket();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {1, 2, 3});
        socket.setKeepAlive(true);
        channel1.write(buffer);
        socket.setOOBInline(true);
        channel1.write(buffer);
        socket.setReceiveBufferSize(100);
        channel1.write(buffer);
        socket.setReuseAddress(true);
        channel1.write(buffer);
        socket.setSendBufferSize(100);
        channel1.write(buffer);
        socket.setSoLinger(true, 100);
        channel1.write(buffer);
        socket.setSoTimeout(1000);
        channel1.write(buffer);
        socket.setTcpNoDelay(true);
        channel1.write(buffer);
        socket.setTrafficClass(10);
        channel1.write(buffer);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    ) 
    public void test_socketChannel_read_DirectByteBuffer() throws InterruptedException, IOException {
        ServerThread server = new ServerThread();
        server.start();
        Thread.currentThread().sleep(1000);
        InetSocketAddress address = new InetSocketAddress(InetAddress
                .getByName("localhost"), port);
        SocketChannel sc = SocketChannel.open();
        sc.connect(address);
        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.limit(data.length / 2);
        sc.read(buf);
        buf.limit(buf.capacity());
        sc.read(buf);
        sc.close();
        buf.rewind();
        assertSameContent(data, buf);
        sc = SocketChannel.open();
        sc.connect(address);
        buf = ByteBuffer.allocateDirect(data.length);
        buf.limit(data.length / 2);
        sc.read(buf);
        buf.limit(buf.capacity());
        sc.read(buf);
        sc.close();
        buf.rewind();
        assertSameContent(data, buf);
    }
    private void assertSameContent(byte[] data, ByteBuffer buf) {
        for (byte b : data) {
            if (b != buf.get()) {
                int pos = buf.position() - 1;
                fail("Content not equal. Buffer position: " +
                        (pos) + " expected: " + b + " was: " + buf.get(pos));
            }
        }
    }
    public static boolean done = false;
    public static int port = Support_PortManager.getNextPort();
    public static byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    static class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                ServerSocketChannel ssc = ServerSocketChannel.open();
                InetSocketAddress addr = new InetSocketAddress(InetAddress
                        .getByAddress(new byte[] {0, 0, 0, 0}), port);
                ssc.socket().bind(addr, 0);
                ByteBuffer buf = ByteBuffer.allocate(10);
                buf.put(data);
                while (!done) {
                    SocketChannel sc = ssc.accept();
                    buf.rewind();
                    sc.write(buf);
                }
            } catch (Exception e) {
            }
        }
    }
    class MockSocketChannel extends SocketChannel {
        private boolean isWriteCalled = false;
        private boolean isReadCalled = false;
        private boolean isConstructorCalled = false;
        public MockSocketChannel(SelectorProvider provider) {
            super(provider);
            isConstructorCalled = true;
        }
        public Socket socket() {
            return null;
        }
        public boolean isConnected() {
            return false;
        }
        public boolean isConnectionPending() {
            return false;
        }
        public boolean connect(SocketAddress address) throws IOException {
            return false;
        }
        public boolean finishConnect() throws IOException {
            return false;
        }
        public int read(ByteBuffer target) throws IOException {
            return 0;
        }
        public long read(ByteBuffer[] targets, int offset, int length)
                throws IOException {
            if (0 == offset && length == targets.length) {
                isReadCalled = true;
            }
            return 0;
        }
        public int write(ByteBuffer source) throws IOException {
            return 0;
        }
        public long write(ByteBuffer[] sources, int offset, int length)
                throws IOException {
            if (0 == offset && length == sources.length) {
                isWriteCalled = true;
            }
            return 0;
        }
        protected void implCloseSelectableChannel() throws IOException {
        }
        protected void implConfigureBlocking(boolean blockingMode)
                throws IOException {
        }
    }
    class SubSocketAddress extends SocketAddress {
        private static final long serialVersionUID = 1L;
        public SubSocketAddress() {
            super();
        }
    }
}
