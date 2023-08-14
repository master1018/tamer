    value = DatagramChannel.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "read",
            args = {java.nio.ByteBuffer[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "read",
            args = {java.nio.ByteBuffer[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "connect",
            args = {java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "read",
            args = {java.nio.ByteBuffer[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer[].class, int.class, int.class}
        )
    }
)
public class DatagramChannelTest extends TestCase {
    private static final int CAPACITY_NORMAL = 200;
    private static final int CAPACITY_1KB = 1024;
    private static final int CAPACITY_64KB = 65536;
    private static final int CAPACITY_ZERO = 0;
    private static final int CAPACITY_ONE = 1;
    private static final int TIME_UNIT = 500;
    private InetSocketAddress localAddr1;
    private InetSocketAddress localAddr2;
    private DatagramChannel channel1;
    private DatagramChannel channel2;
    private DatagramSocket datagramSocket1;
    private DatagramSocket datagramSocket2;
    private int testPort;
    protected void setUp() throws Exception {
        super.setUp();
        this.channel1 = DatagramChannel.open();
        this.channel2 = DatagramChannel.open();
        int[] ports = Support_PortManager.getNextPortsForUDP(5);
        this.localAddr1 = new InetSocketAddress("127.0.0.1", ports[0]);
        this.localAddr2 = new InetSocketAddress("127.0.0.1", ports[1]);
        this.datagramSocket1 = new DatagramSocket(ports[2]);
        this.datagramSocket2 = new DatagramSocket(ports[3]);
        testPort = ports[4];
    }
    protected void tearDown() throws Exception {
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
        if (null != this.datagramSocket1) {
            try {
                this.datagramSocket1.close();
            } catch (Exception e) {
            }
        }
        if (null != this.datagramSocket2) {
            try {
                this.datagramSocket2.close();
            } catch (Exception e) {
            }
        }
        localAddr1 = null;
        localAddr2 = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DatagramChannel",
        args = {java.nio.channels.spi.SelectorProvider.class}
    )
    public void testConstructor() throws IOException {
        DatagramChannel channel =
                SelectorProvider.provider().openDatagramChannel();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(),channel.provider());
        channel = DatagramChannel.open();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(), channel.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "validOps",
        args = {}
    )
    public void testValidOps() {
        MockDatagramChannel testMock = new MockDatagramChannel(SelectorProvider
                .provider());
        MockDatagramChannel testMocknull = new MockDatagramChannel(null);
        int val = this.channel1.validOps();
        assertEquals(5, val);
        assertEquals(val, testMock.validOps());
        assertEquals(val, testMocknull.validOps());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the result of the setUp method.",
        method = "open",
        args = {}
    )
    public void testOpen() {
        MockDatagramChannel testMock = new MockDatagramChannel(SelectorProvider
                .provider());
        MockDatagramChannel testMocknull = new MockDatagramChannel(null);
        assertNull(testMocknull.provider());
        assertNotNull(testMock.provider());
        assertEquals(this.channel1.provider(), testMock.provider());
        assertEquals(5, testMock.validOps());
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of DatagramChannel.",
            method = "validOps",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of DatagramChannel.",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of DatagramChannel.",
            method = "isRegistered",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of DatagramChannel.",
            method = "isBlocking",
            args = {}
        )
    })
    public void testChannelBasicStatus() {
        DatagramSocket gotSocket = this.channel1.socket();
        assertFalse(gotSocket.isClosed());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isRegistered());
        assertEquals((SelectionKey.OP_READ | SelectionKey.OP_WRITE),
                this.channel1.validOps());
        assertEquals(SelectorProvider.provider(), this.channel1.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testReadByteBufferArray() throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.read(readBuf);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertEquals(0, this.channel1.read(readBuf));
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            assertEquals(0, this.channel1.read(readBuf));
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testReadByteBufferArray_ConnectedBufNull()
            throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        try {
            this.channel1.read((ByteBuffer[])null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.read(readBuf);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        datagramSocket1.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testReadByteBufferArray_NotConnectedBufNull()
            throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.read((ByteBuffer[])null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.read(readBuf);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify all exceptions according to specification.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_Block() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.write(writeBuf);
            fail("Should throw NotYetConnectedException.");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf));
        assertEquals(0, this.channel1.write(writeBuf));
    }
    public void disabled_testWriteByteBufferArray_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer[] targetBuf = new ByteBuffer[2];
        targetBuf[0] = ByteBuffer.wrap(new byte[2]);
        targetBuf[1] = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.write(targetBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_testWriteByteBufferArray_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer[] targetBuf = new ByteBuffer[2];
                    targetBuf[0] = ByteBuffer.wrap(new byte[2]);
                    targetBuf[1] = ByteBuffer.wrap(new byte[2]);
                    channel1.write(targetBuf);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_NonBlock() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.configureBlocking(false);
        try {
            this.channel1.write(writeBuf);
            fail("Should throw NotYetConnectedException.");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf));
        assertEquals(0, this.channel1.write(writeBuf));
        this.channel1.close();
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw ClosedChannelEception.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_BlockClosed() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.configureBlocking(false);
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_NonBlockClosed() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_NotConnectedBufNull()
            throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        try {
            this.channel1.write((ByteBuffer[])null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.write(writeBuf);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testWriteByteBufferArray_ConnectedBufNull()
            throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        try {
            this.channel1.write((ByteBuffer[])null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.write(writeBuf);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        datagramSocket1.close();
        try {
            this.channel1.write((ByteBuffer[])null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_BasicStatusBeforeConnect() throws SocketException {
        assertFalse(this.channel1.isConnected());
        DatagramSocket s1 = this.channel1.socket();
        assertSocketBeforeConnect(s1);
        DatagramSocket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_Block_BasicStatusAfterConnect() throws IOException {
        this.channel1.connect(localAddr1);
        DatagramSocket s1 = this.channel1.socket();
        assertSocketAfterConnect(s1);
        DatagramSocket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_NonBlock_BasicStatusAfterConnect()
            throws IOException {
        this.channel1.connect(localAddr1);
        this.channel1.configureBlocking(false);
        DatagramSocket s1 = this.channel1.socket();
        assertSocketAfterConnect(s1);
        DatagramSocket s2 = this.channel1.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_ActionsBeforeConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        DatagramSocket s = this.channel1.socket();
        assertSocketActionBeforeConnect(s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_Block_ActionsAfterConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        this.channel1.connect(localAddr1);
        DatagramSocket s = this.channel1.socket();
        assertSocketActionAfterConnect(s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_NonBlock_ActionsAfterConnect() throws IOException {
        this.channel1.connect(localAddr1);
        this.channel1.configureBlocking(false);
        DatagramSocket s = this.channel1.socket();
        assertSocketActionAfterConnect(s);
    }
    private void assertSocketBeforeConnect(DatagramSocket s)
            throws SocketException {
        assertFalse(s.isBound());
        assertFalse(s.isClosed());
        assertFalse(s.isConnected());
        assertFalse(s.getBroadcast());
        assertFalse(s.getReuseAddress());
        assertNull(s.getInetAddress());
        assertTrue(s.getLocalAddress().isAnyLocalAddress());
        assertEquals(s.getLocalPort(), 0);
        assertNull(s.getLocalSocketAddress());
        assertEquals(s.getPort(), -1);
        assertTrue(s.getReceiveBufferSize() >= 8192);
        assertNull(s.getRemoteSocketAddress());
        assertFalse(s.getReuseAddress());
        assertTrue(s.getSendBufferSize() >= 8192);
        assertEquals(s.getSoTimeout(), 0);
        assertEquals(s.getTrafficClass(), 0);
    }
    private void assertSocketAfterConnect(DatagramSocket s)
            throws SocketException {
        assertTrue(s.isBound());
        assertFalse(s.isClosed());
        assertTrue(s.isConnected());
        assertFalse(s.getBroadcast());
        assertFalse(s.getReuseAddress());
        assertSame(s.getInetAddress(), localAddr1.getAddress());
        assertEquals(s.getLocalAddress(), localAddr1.getAddress());
        assertNotNull(s.getLocalSocketAddress());
        assertEquals(s.getPort(), localAddr1.getPort());
        assertTrue(s.getReceiveBufferSize() >= 8192);
        assertNotSame(s.getRemoteSocketAddress(), localAddr1);
        assertEquals(s.getRemoteSocketAddress(), localAddr1);
        assertFalse(s.getReuseAddress());
        assertTrue(s.getSendBufferSize() >= 8192);
        assertEquals(s.getSoTimeout(), 0);
        assertEquals(s.getTrafficClass(), 0);
    }
    private void assertSocketActionBeforeConnect(DatagramSocket s)
            throws IOException {
        s.connect(localAddr2);
        assertFalse(this.channel1.isConnected());
        assertFalse(s.isConnected());
        s.disconnect();
        assertFalse(this.channel1.isConnected());
        assertFalse(s.isConnected());
        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }
    private void assertSocketActionAfterConnect(DatagramSocket s)
            throws IOException {
        assertEquals(s.getPort(), localAddr1.getPort());
        s.connect(localAddr2);
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());
        assertEquals(s.getPort(), localAddr1.getPort());
        s.disconnect();
        assertFalse(this.channel1.isConnected());
        assertFalse(s.isConnected());
        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {ByteBuffer.class}
    )
    public void testConfigureBlocking_Read() throws Exception {
        assertTrue(this.channel1.isBlocking());
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_1KB);
        new Thread() {
            public void run() {
                try {
                    sleep(TIME_UNIT * 5);
                    channel1.configureBlocking(false);
                    assertFalse(channel1.isBlocking());
                    datagramSocket1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        SocketAddress addr = channel1.receive(buf);
        assertNull(addr);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isConnected",
        args = {}
    )
    public void testIsConnected_WithServer() throws IOException {
        connectLocalServer();
        assertTrue(this.channel1.isConnected());
        disconnectAfterConnected();
        this.datagramSocket1.close();
        this.channel1.close();
        assertFalse(this.channel1.isConnected());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_BlockWithServer() throws IOException {
        assertTrue(this.channel1.isBlocking());
        connectLocalServer();
        datagramSocket1.close();
        disconnectAfterConnected();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_BlockNoServer() throws IOException {
        connectWithoutServer();
        disconnectAfterConnected();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_NonBlockWithServer() throws IOException {
        this.channel1.configureBlocking(false);
        connectLocalServer();
        datagramSocket1.close();
        disconnectAfterConnected();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_Null() throws IOException {
        assertFalse(this.channel1.isConnected());
        try {
            this.channel1.connect(null);
            fail("Should throw an IllegalArgumentException here."); 
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedAddressTypeException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_UnsupportedType() throws IOException {
        assertFalse(this.channel1.isConnected());
        class SubSocketAddress extends SocketAddress {
            private static final long serialVersionUID = 1L;
            public SubSocketAddress() {
                super();
            }
        }
        SocketAddress newTypeAddress = new SubSocketAddress();
        try {
            this.channel1.connect(newTypeAddress);
            fail("Should throw an UnsupportedAddressTypeException here.");
        } catch (UnsupportedAddressTypeException e) {
        }
    }
    public void disabled_testConnect_Block_close() throws Exception {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.connect(localAddr1);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_testConnect_Block_interrupt() throws Exception {
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    channel1.connect(localAddr1);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnresolvedAddressException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_Unresolved() throws IOException {
        assertFalse(this.channel1.isConnected());
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
    public void testConnect_EmptyHost() throws Exception {
        assertFalse(this.channel1.isConnected());
        assertEquals(this.channel1, this.channel1
                .connect(new InetSocketAddress("", 1081))); 
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_ClosedChannelException() throws IOException {
        assertFalse(this.channel1.isConnected());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ClosedChannelException."); 
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalStateException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_IllegalStateException() throws IOException {
        assertFalse(this.channel1.isConnected());
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isConnected());
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw IllegalStateException."); 
        } catch (IllegalStateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_CheckOpenBeforeStatus() throws IOException {
        assertFalse(this.channel1.isConnected());
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ClosedChannelException."); 
        } catch (ClosedChannelException e) {
        }
    }
    private void disconnectAfterConnected() throws IOException {
        assertTrue(this.channel1.isConnected());
        this.channel1.disconnect();
        assertFalse(this.channel1.isConnected());
    }
    private void disconnectAfterClosed() throws IOException {
        assertFalse(this.channel1.isOpen());
        assertFalse(this.channel1.isConnected());
        this.channel1.disconnect();
        assertFalse(this.channel1.isConnected());
    }
    private void connectLocalServer() throws IOException {
        assertFalse(this.channel1.isConnected());
        assertTrue(this.datagramSocket1.isBound());
        assertSame(this.channel1, this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnected());
    }
    private void connectWithoutServer() throws IOException {
        assertFalse(this.channel1.isConnected());
        this.datagramSocket1.close();
        assertTrue(this.datagramSocket1.isClosed());
        assertSame(this.channel1, this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnected());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify IOException.",
        method = "disconnect",
        args = {}
    )
    public void testDisconnect_BeforeConnect() throws IOException {
        assertFalse(this.channel1.isConnected());
        assertEquals(this.channel1, this.channel1.disconnect());
        assertFalse(this.channel1.isConnected());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void testDisconnect_UnconnectedClosed() throws IOException {
        assertFalse(this.channel1.isConnected());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        assertEquals(this.channel1, this.channel1.disconnect());
        assertFalse(this.channel1.isConnected());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void testDisconnect_BlockWithServerChannelClosed()
            throws IOException {
        assertTrue(this.channel1.isBlocking());
        connectLocalServer();
        this.channel1.close();
        disconnectAfterClosed();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void testDisconnect_NonBlockWithServerChannelClosed()
            throws IOException {
        this.channel1.configureBlocking(false);
        connectLocalServer();
        this.channel1.close();
        disconnectAfterClosed();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void testDisconnect_BlockWithServerServerClosed()
            throws IOException {
        assertTrue(this.channel1.isBlocking());
        connectLocalServer();
        this.datagramSocket1.close();
        assertTrue(this.channel1.isOpen());
        assertTrue(this.channel1.isConnected());
        disconnectAfterConnected();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "disconnect",
        args = {}
    )
    public void testDisconnect_NonBlockWithServerServerClosed()
            throws IOException {
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.isBlocking());
        connectLocalServer();
        this.datagramSocket1.close();
        assertTrue(this.channel1.isOpen());
        assertTrue(this.channel1.isConnected());
        disconnectAfterConnected();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedNull() throws Exception {
        assertFalse(this.channel1.isConnected());
        try {
            this.channel1.receive(null);
            fail("Should throw a NPE here."); 
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedReadonly() throws Exception {
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL)
                .asReadOnlyBuffer();
        assertTrue(dst.isReadOnly());
        try {
            this.channel1.receive(dst);
            fail("Should throw an IllegalArgumentException here."); 
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedBufEmpty() throws Exception {
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        assertNull(this.channel1.receive(dst));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedBufZero() throws Exception {
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_ZERO);
        assertNull(this.channel1.receive(dst));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedBufNotEmpty() throws Exception {
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        dst.put((byte) 88);
        assertEquals(dst.position() + CAPACITY_NORMAL - 1, dst.limit());
        assertNull(this.channel1.receive(dst));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedBufFull() throws Exception {
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_ONE);
        dst.put((byte) 88);
        assertEquals(dst.position(), dst.limit());
        assertNull(this.channel1.receive(dst));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedClose() throws Exception {
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            assertNull(this.channel1.receive(dst));
            fail("Should throw a ClosedChannelException here."); 
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedCloseNull() throws Exception {
        assertFalse(this.channel1.isConnected());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            this.channel1.receive(null);
            fail("Should throw a NPE here."); 
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_UnconnectedCloseReadonly() throws Exception {
        assertFalse(this.channel1.isConnected());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL)
                .asReadOnlyBuffer();
        assertTrue(dst.isReadOnly());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            this.channel1.receive(dst);
            fail("Should throw an IllegalArgumentException here."); 
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerBufEmpty() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNonBlockNoServer(CAPACITY_NORMAL);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_BlockNoServerNull() throws Exception {
        assertTrue(this.channel1.isBlocking());
        receiveNoServerNull();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerNull() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNoServerNull();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_BlockNoServerReadonly() throws Exception {
        assertTrue(this.channel1.isBlocking());
        receiveNoServerReadonly();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerReadonly() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNoServerReadonly();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerBufZero() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNonBlockNoServer(CAPACITY_ZERO);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerBufNotEmpty() throws Exception {
        this.channel1.configureBlocking(false);
        connectWithoutServer();
        ByteBuffer dst = allocateNonEmptyBuf();
        assertNull(this.channel1.receive(dst));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerBufFull() throws Exception {
        this.channel1.configureBlocking(false);
        connectWithoutServer();
        ByteBuffer dst = allocateFullBuf();
        assertNull(this.channel1.receive(dst));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_BlockNoServerChannelClose() throws Exception {
        assertTrue(this.channel1.isBlocking());
        receiveNoServerChannelClose();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerChannelClose() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNoServerChannelClose();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_BlockNoServerCloseNull() throws Exception {
        assertTrue(this.channel1.isBlocking());
        receiveNoServerChannelCloseNull();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerCloseNull() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNoServerChannelCloseNull();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_NonBlockNoServerCloseReadonly() throws Exception {
        this.channel1.configureBlocking(false);
        receiveNoServerChannelCloseReadonly();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_BlockNoServerCloseReadonly() throws Exception {
        assertTrue(this.channel1.isBlocking());
        receiveNoServerChannelCloseReadonly();
    }
    private void receiveNoServerNull() throws IOException {
        connectWithoutServer();
        try {
            this.channel1.receive(null);
            fail("Should throw a NPE here."); 
        } catch (NullPointerException e) {
        }
    }
    private void receiveNoServerReadonly() throws IOException {
        connectWithoutServer();
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL)
                .asReadOnlyBuffer();
        assertTrue(dst.isReadOnly());
        try {
            this.channel1.receive(dst);
            fail("Should throw an IllegalArgumentException here."); 
        } catch (IllegalArgumentException e) {
        }
    }
    private void receiveNonBlockNoServer(int size) throws IOException {
        connectWithoutServer();
        ByteBuffer dst = ByteBuffer.allocateDirect(size);
        assertNull(this.channel1.receive(dst));
    }
    private void receiveNoServerChannelClose() throws IOException {
        connectWithoutServer();
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            assertNull(this.channel1.receive(dst));
            fail("Should throw a ClosedChannelException here."); 
        } catch (ClosedChannelException e) {
        }
    }
    private void receiveNoServerChannelCloseNull() throws IOException {
        connectWithoutServer();
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            this.channel1.receive(null);
            fail("Should throw a NPE here."); 
        } catch (NullPointerException e) {
        }
    }
    private void receiveNoServerChannelCloseReadonly() throws IOException {
        connectWithoutServer();
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL)
                .asReadOnlyBuffer();
        assertTrue(dst.isReadOnly());
        try {
            this.channel1.receive(dst);
            fail("Should throw an IllegalArgumentException here."); 
        } catch (IllegalArgumentException e) {
        }
    }
    private ByteBuffer allocateFullBuf() {
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_ONE);
        dst.put((byte) 88);
        assertEquals(dst.position(), dst.limit());
        return dst;
    }
    private ByteBuffer allocateNonEmptyBuf() {
        ByteBuffer dst = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        dst.put((byte) 88);
        dst.put((byte) 99);
        assertEquals(dst.position() + CAPACITY_NORMAL - 2, dst.limit());
        return dst;
    }
    private void sendDataBlocking(InetSocketAddress addr, ByteBuffer writeBuf)
            throws IOException {
        InetSocketAddress ipAddr = addr;
        assertEquals(CAPACITY_NORMAL, this.channel1.send(writeBuf, ipAddr));
        assertTrue(this.channel1.isOpen());
        assertTrue(this.channel1.isBlocking());
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
    }
    private void sendDataNonBlocking(InetSocketAddress addr, ByteBuffer writeBuf)
            throws IOException {
        InetSocketAddress ipAddr = addr;
        this.channel1.configureBlocking(false);
        assertEquals(CAPACITY_NORMAL, this.channel1.send(writeBuf, ipAddr));
        assertTrue(this.channel1.isOpen());
        assertFalse(this.channel1.isBlocking());
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerBlockingCommon() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        sendDataBlocking(localAddr1, writeBuf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerNonblockingCommon() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        sendDataNonBlocking(localAddr1, writeBuf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerTwice() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        sendDataBlocking(localAddr1, writeBuf);
        assertEquals(0, this.channel1.send(writeBuf, localAddr1));
        try {
            channel1.send(writeBuf, localAddr2);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerNonBlockingTwice() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        sendDataNonBlocking(localAddr1, writeBuf);
        assertEquals(0, this.channel1.send(writeBuf, localAddr1));
        try {
            channel1.send(writeBuf, localAddr2);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerBufNull() throws IOException {
        try {
            sendDataBlocking(localAddr1, null);
            fail("Should throw a NPE here.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerBufNullTwice() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        try {
            sendDataBlocking(localAddr1, null);
            fail("Should throw a NPE here.");
        } catch (NullPointerException e) {
        }
        sendDataBlocking(localAddr1, writeBuf);
        try {
            channel1.send(null, localAddr2);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "DOesn't verify all exceptions according to spec.",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerAddrNull() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        try {
            sendDataBlocking(null, writeBuf);
            fail("Should throw a NPE here.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_NoServerAddrNullTwice() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        try {
            sendDataBlocking(null, writeBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
        sendDataBlocking(localAddr1, writeBuf);
        try {
            channel1.send(writeBuf, null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Normal() throws Exception {
        this.channel1.socket().bind(localAddr2);
        sendByChannel("some normal string in testReceiveSend_Normal",
                localAddr2);
        receiveByChannel(CAPACITY_NORMAL, localAddr2,
                "some normal string in testReceiveSend_Normal");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_NotBound() throws Exception {
        sendByChannel("some normal string in testReceiveSend_Normal",
                localAddr2);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        assertNull(channel1.receive(buf));
        assertFalse(channel1.socket().isBound());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_NonBlock_NotBound() throws Exception {
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        sendByChannel("some normal string in testReceiveSend_Normal",
                localAddr2);
        ByteBuffer buf = ByteBuffer.wrap(new byte[CAPACITY_NORMAL]);
        assertNull(this.channel1.receive(buf));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Normal_S2C() throws Exception {
        this.channel1.socket().bind(localAddr2);
        sendByDatagramSocket(
                "some normal string in testReceiveSend_Normal_S2C", localAddr2);
        receiveByChannel(CAPACITY_NORMAL, localAddr2,
                "some normal string in testReceiveSend_Normal_S2C");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Normal_C2S() throws Exception {
        this.datagramSocket1 = new DatagramSocket(localAddr2.getPort());
        String str1 = "some normal string in testReceiveSend_Normal_C2S";
        sendByChannel(str1, localAddr2);
        receiveByDatagramSocket(CAPACITY_NORMAL, localAddr2, str1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_NonBlock_Normal_C2S() throws Exception {
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.datagramSocket1 = new DatagramSocket(localAddr2.getPort());
        String str1 = "some normal string in testReceiveSend_Normal_C2S";
        sendByChannel(str1, localAddr2);
        receiveByDatagramSocket(CAPACITY_NORMAL, localAddr2, str1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Normal_S2S() throws Exception {
        String msg = "normal string in testReceiveSend_Normal_S2S";
        this.datagramSocket1 = new DatagramSocket(testPort);
        DatagramPacket rdp = new DatagramPacket(msg.getBytes(), msg.length(),
                localAddr2);
        datagramSocket2 = new DatagramSocket(localAddr2.getPort());
        this.datagramSocket1.send(rdp);
        byte[] buf = new byte[CAPACITY_NORMAL];
        this.datagramSocket2.setSoTimeout(TIME_UNIT);
        rdp = new DatagramPacket(buf, buf.length);
        this.datagramSocket2.receive(rdp);
        assertEquals(new String(buf, 0, CAPACITY_NORMAL).trim(), msg);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Empty() throws Exception {
        this.channel1.socket().bind(localAddr2);
        sendByChannel("", localAddr2);
        receiveByChannel(CAPACITY_NORMAL, localAddr2, "");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_NonBlock_Empty() throws Exception {
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        sendByChannel("", localAddr2);
        receiveByChannel(CAPACITY_NORMAL, localAddr2, "");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Empty_S2C() throws Exception {
        this.channel1.socket().bind(localAddr2);
        sendByDatagramSocket("", localAddr2);
        receiveByChannel(CAPACITY_NORMAL, localAddr2, "");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_NonBlock_Empty_S2C() throws Exception {
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        sendByDatagramSocket("", localAddr2);
        receiveByChannel(CAPACITY_NORMAL, localAddr2, "");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Empty_C2S() throws Exception {
        this.datagramSocket1 = new DatagramSocket(localAddr2.getPort());
        sendByChannel("", localAddr2);
        receiveByDatagramSocket(CAPACITY_NORMAL, localAddr2, "");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_NonBlock_Empty_C2S() throws Exception {
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.datagramSocket1 = new DatagramSocket(localAddr2.getPort());
        sendByChannel("", localAddr2);
        receiveByDatagramSocket(CAPACITY_NORMAL, localAddr2, "");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceiveSend_Empty_S2S() throws Exception {
        String msg = "";
        this.datagramSocket1 = new DatagramSocket(testPort);
        DatagramPacket rdp = new DatagramPacket(msg.getBytes(), msg.length(),
                localAddr2);
        datagramSocket2 = new DatagramSocket(localAddr2.getPort());
        this.datagramSocket1.send(rdp);
        byte[] buf = new byte[CAPACITY_NORMAL];
        this.datagramSocket2.setSoTimeout(TIME_UNIT);
        rdp = new DatagramPacket(buf, buf.length);
        this.datagramSocket2.receive(rdp);
        assertEquals(new String(buf, 0, CAPACITY_NORMAL).trim(), msg);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Oversize() throws Exception {
        this.channel1.socket().bind(localAddr2);
        sendByChannel("0123456789", localAddr2);
        receiveByChannel(5, localAddr2, "01234");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Oversize_C2S() throws Exception {
        this.datagramSocket1 = new DatagramSocket(localAddr2.getPort());
        sendByChannel("0123456789", localAddr2);
        receiveByDatagramSocket(5, localAddr2, "01234");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_NonBlock_Oversize_C2S() throws Exception {
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.datagramSocket1 = new DatagramSocket(localAddr2.getPort());
        sendByChannel("0123456789", localAddr2);
        receiveByDatagramSocket(5, localAddr2, "01234");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_Block_Oversize_S2C() throws Exception {
        this.channel1.socket().bind(localAddr2);
        sendByDatagramSocket("0123456789", localAddr2);
        receiveByChannel(5, localAddr2, "01234");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "receive",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "send",
            args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
        )
    })
    public void testReceiveSend_8K() throws Exception {
        StringBuffer str8k = new StringBuffer();
        for (int i = 0; i < 8 * CAPACITY_1KB; i++) {
            str8k.append("a");
        }
        String str = str8k.toString();
        this.channel1.socket().bind(localAddr2);
        sendByChannel(str, localAddr2);
        receiveByChannel(8 * CAPACITY_1KB, localAddr2, str);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testReceiveSend_64K() throws Exception {
        StringBuffer str64k = new StringBuffer();
        for (int i = 0; i < CAPACITY_64KB; i++) {
            str64k.append("a");
        }
        String str = str64k.toString();
        try {
            Thread.sleep(TIME_UNIT);
            channel2.send(ByteBuffer.wrap(str.getBytes()), localAddr1);
            fail("Should throw SocketException!");
        } catch (SocketException e) {
        }
    }
    private void sendByChannel(String data, InetSocketAddress address)
            throws Exception {
        try {
            assertEquals(data.length(), this.channel2.send(ByteBuffer.wrap(data
                    .getBytes()), address));
        } finally {
            this.channel2.close();
        }
    }
    private void sendByDatagramSocket(String data, InetSocketAddress address)
            throws Exception {
        this.datagramSocket1 = new DatagramSocket(testPort);
        DatagramPacket rdp = new DatagramPacket(data.getBytes(), data.length(),
                address);
        this.datagramSocket1.send(rdp);
    }
    private void receiveByChannel(int bufSize, InetSocketAddress address,
            String expectedString) throws IOException {
        try {
            ByteBuffer buf = ByteBuffer.wrap(new byte[bufSize]);
            InetSocketAddress returnAddr = null;
            long startTime = System.currentTimeMillis();
            do {
                returnAddr = (InetSocketAddress) this.channel1.receive(buf);
                if (channel1.isBlocking() || null != returnAddr) {
                    break;
                }
                assertTimeout(startTime, 10000);
            } while (true);
            int length = returnAddr.getAddress().getAddress().length;
            for (int i = 0; i < length; i++) {
                assertEquals(returnAddr.getAddress().getAddress()[i],
                        InetAddress.getByName("127.0.0.1").getAddress()[i]);
            }
            assertFalse(returnAddr.getPort() == address.getPort());
            assertEquals(new String(buf.array(), 0, bufSize).trim(),
                    expectedString);
        } finally {
            this.channel1.close();
        }
    }
    private void assertTimeout(long startTime, long timeout) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - startTime) > timeout) {
            fail("Timeout");
        }
    }
    private void receiveByDatagramSocket(int bufSize,
            InetSocketAddress address, String expectedString)
            throws IOException {
        byte[] buf = new byte[bufSize];
        this.datagramSocket1.setSoTimeout(6000);
        DatagramPacket rdp = new DatagramPacket(buf, buf.length);
        this.datagramSocket1.receive(rdp);
        assertEquals(new String(buf, 0, bufSize).trim(), expectedString);
    }
    private class mockAddress extends SocketAddress {
        private static final long serialVersionUID = 1L;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_MockSocketAddress() throws Exception {
        SocketAddress badAddr = new mockAddress();
        final SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            this.channel1
                    .send(ByteBuffer.allocate(CAPACITY_NORMAL), localAddr1);
            this.channel1.close();
            this.channel1 = DatagramChannel.open();
            try {
                this.channel1.send(ByteBuffer.allocate(CAPACITY_NORMAL),
                        badAddr);
                fail("Should throw ClassCastException");
            } catch (ClassCastException e) {
            }
        } finally {
            System.setSecurityManager(sm);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify AsynchronousCloseException, ClosedByInterruptException, IOException.",
        method = "send",
        args = {java.nio.ByteBuffer.class, SocketAddress.class}
    )
    public void testSend_Security() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        String strHello = "hello";
        localAddr1 = new InetSocketAddress("127.0.0.1", testPort);
        this.channel1.socket().bind(localAddr1);
        this.channel2.socket().bind(localAddr2);
        this.channel1.connect(localAddr2);
        final SecurityManager sm = System.getSecurityManager();
        MockSecurityManager mockManager = new MockSecurityManager("127.0.0.1");
        System.setSecurityManager(mockManager);
        try {
            this.channel2.send(ByteBuffer.wrap(strHello.getBytes()), localAddr1);
            assertEquals(strHello.length(), this.channel1.read(buf));
        } finally {
            System.setSecurityManager(sm);
        }
        assertTrue(mockManager.checkConnectCalled);
    }
    public void disabled_testSend_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.send(targetBuf, localAddr1);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_testSend_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
                    channel1.send(targetBuf, localAddr1);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_Security() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        String strHello = "hello";
        localAddr1 = new InetSocketAddress("127.0.0.1", testPort);
        this.channel1.socket().bind(localAddr1);
        sendByChannel(strHello, localAddr1);
        final SecurityManager sm = System.getSecurityManager();
        MockSecurityManager mockManager = new MockSecurityManager("10.0.0.1");
        System.setSecurityManager(mockManager);
        try {
            this.channel1.configureBlocking(false);
            assertNull(this.channel1.receive(buf));
        } finally {
            System.setSecurityManager(sm);
        }
        assertTrue(mockManager.checkAcceptCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.receive(targetBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "receive",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReceive_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
                    channel1.receive(targetBuf);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "connect",
        args = {java.net.SocketAddress.class}
    )
    public void testConnect_Security() throws IOException {
        localAddr1 = new InetSocketAddress("127.0.0.1", testPort);
        SecurityManager sm = System.getSecurityManager();
        MockSecurityManager mockManager = new MockSecurityManager("127.0.0.1");
        System.setSecurityManager(mockManager);
        try {
            this.channel1.connect(localAddr1);
        } finally {
            System.setSecurityManager(sm);
        }
        assertTrue(mockManager.checkConnectCalled);
    }
    private void connectWriteBuf(InetSocketAddress ipAddr, ByteBuffer buf)
            throws IOException {
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(buf));
        assertEquals(0, this.channel1.write(buf));
    }
    private void noconnectWrite(ByteBuffer buf) throws IOException {
        try {
            this.channel1.write(buf);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_Block() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        connectWriteBuf(localAddr1, writeBuf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_NonBlock() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        this.channel1.configureBlocking(false);
        connectWriteBuf(localAddr1, writeBuf);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_BlockClosed() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        noconnectWrite(writeBuf);
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_NonBlockClosed() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.configureBlocking(false);
        noconnectWrite(writeBuf);
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_BlockBufNull() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(0);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.write((ByteBuffer) null);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        try {
            this.channel1.write((ByteBuffer) null);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
        assertEquals(0, this.channel1.write(writeBuf));
        datagramSocket1.close();
        try {
            this.channel1.write((ByteBuffer) null);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_NonBlockBufNull() throws IOException {
        ByteBuffer writeBuf = ByteBuffer.allocateDirect(0);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.configureBlocking(false);
        try {
            this.channel1.write((ByteBuffer) null);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        try {
            this.channel1.write((ByteBuffer) null);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
        assertEquals(0, this.channel1.write(writeBuf));
        datagramSocket1.close();
        try {
            this.channel1.write((ByteBuffer) null);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify all exceptions according to specification.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_Block() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("Should throw NotYetConnectedException.");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf, 0, 2));
        assertEquals(0, this.channel1.write(writeBuf, 0, 1));
        this.channel1.close();
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw ClosedChannelEception.");
        } catch (ClosedChannelException e) {
        }
    }
    public void disabled_testWriteByteBufferArrayII_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer[] targetBuf = new ByteBuffer[2];
        targetBuf[0] = ByteBuffer.wrap(new byte[2]);
        targetBuf[1] = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.write(targetBuf, 0 ,2);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_testWriteByteBufferArrayII_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer[] targetBuf = new ByteBuffer[2];
                    targetBuf[0] = ByteBuffer.wrap(new byte[2]);
                    targetBuf[1] = ByteBuffer.wrap(new byte[2]);
                    channel1.write(targetBuf, 0, 2);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_NonBlock() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.configureBlocking(false);
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("Should throw NotYetConnectedException.");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf, 0, 2));
        assertEquals(0, this.channel1.write(writeBuf, 0, 1));
        this.channel1.close();
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw ClosedChannelEception.");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_BlockClosed() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.configureBlocking(false);
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        try {
            channel1.write(writeBuf, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_NonBlockClosed() throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        try {
            channel1.write(writeBuf, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_NotConnectedIndexBad()
            throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.write(writeBuf, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_ConnectedIndexBad()
            throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        try {
            this.channel1.write(writeBuf, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(writeBuf, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_NotConnectedBufNull()
            throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        try {
            this.channel1.write(null, 0, 20);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testWriteByteBufferArrayII_ConnectedBufNull()
            throws IOException {
        ByteBuffer[] writeBuf = new ByteBuffer[2];
        writeBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        try {
            this.channel1.write(null, 0, 20);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        datagramSocket1.close();
        try {
            this.channel1.write(null, 0, 20);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        try {
            this.channel1.read(readBuf);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertEquals(0, this.channel1.read(readBuf));
        this.channel1.close();
        try {
            this.channel1.read(readBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_BufNull() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocateDirect(0);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.read(readBuf);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        try {
            channel1.read((ByteBuffer) null);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        this.channel1.configureBlocking(false);
        assertEquals(0, this.channel1.read(readBuf));
        datagramSocket1.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify AsynchronousCloseException, ClosedByInterruptException, IOException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII() throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.read(readBuf, 0, 2);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        assertEquals(0, this.channel1.read(readBuf, 0, 1));
        assertEquals(0, this.channel1.read(readBuf, 0, 2));
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
        try {
            assertEquals(0, this.channel1.read(readBuf, 0, 1));
        } catch (ClosedChannelException e) {
        }
        datagramSocket1.close();
        try {
            DatagramChannel.open().read(new ByteBuffer[] {}, 2, Integer.MAX_VALUE);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DatagramChannel.open().write(new ByteBuffer[] {}, 2, Integer.MAX_VALUE);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            DatagramChannel.open().write((ByteBuffer[])null, -1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII_ConnectedBufNull()
            throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        try {
            this.channel1.read(null, 0, 2);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        assertEquals(0, this.channel1.read(readBuf, 0, 1));
        try {
            this.channel1.read(readBuf, 0, 2);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        datagramSocket1.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII_NotConnectedBufNull()
            throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.read(null, 0, 2);
            fail("should throw NPE");
        } catch (NullPointerException e) {
        }
        try {
            this.channel1.read(readBuf, 0, 2);
            fail("should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII_ConnectedIndexBad() throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        this.channel1.connect(ipAddr);
        assertTrue(this.channel1.isConnected());
        this.channel1.configureBlocking(false);
        try {
            this.channel1.read(readBuf, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.read(readBuf, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.read(readBuf, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.read(readBuf, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.read(readBuf, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.read(readBuf, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII_NotConnectedIndexBad()
            throws IOException {
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        InetSocketAddress ipAddr = localAddr1;
        try {
            this.channel1.write(readBuf, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(readBuf, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(readBuf, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(readBuf, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(readBuf, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            this.channel1.write(readBuf, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    public void disabled_testReadByteBufferArrayII_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer[] targetBuf = new ByteBuffer[2];
        targetBuf[0] = ByteBuffer.wrap(new byte[2]);
        targetBuf[1] = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.read(targetBuf, 0, 2);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer[] targetBuf = new ByteBuffer[2];
                    targetBuf[0] = ByteBuffer.wrap(new byte[2]);
                    targetBuf[1] = ByteBuffer.wrap(new byte[2]);
                    channel1.read(targetBuf, 0, 2);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadWrite_configureBlock() throws Exception {
        byte[] targetArray = new byte[2];
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.configureBlocking(false);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.read(targetBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
        this.channel1.close();
        try {
            this.channel1.configureBlocking(true);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        this.channel1 = SelectorProvider.provider().openDatagramChannel();
        this.channel1.configureBlocking(false);
        this.channel1.register(SelectorProvider.provider().openSelector(),
                SelectionKey.OP_READ);
        try {
            this.channel1.configureBlocking(true);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_Zero() throws Exception {
        byte[] sourceArray = new byte[0];
        byte[] targetArray = new byte[0];
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(0, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        assertEquals(0, this.channel2.read(targetBuf));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_Normal() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        readWriteReadData(this.channel1, sourceArray, this.channel2,
                targetArray, CAPACITY_NORMAL, "testReadWrite_Block_Normal");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_Empty() throws Exception {
        byte[] sourceArray = "".getBytes();
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(0, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        closeBlockedReaderChannel2(targetBuf);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_changeBlock_Empty() throws Exception {
        byte[] sourceArray = "".getBytes();
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(0, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel2.configureBlocking(false);
                    Thread.sleep(TIME_UNIT * 5);
                    channel2.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            assertTrue(this.channel2.isBlocking());
            this.channel2.read(targetBuf);
            fail("Should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
            assertFalse(this.channel2.isBlocking());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_8KB() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_1KB * 8];
        byte[] targetArray = new byte[CAPACITY_1KB * 8];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        readWriteReadData(this.channel1, sourceArray, this.channel2,
                targetArray, 8 * CAPACITY_1KB, "testReadWrite_Block_8KB");
    }
    private void readWriteReadData(DatagramChannel sender, byte[] sourceArray,
            DatagramChannel receiver, byte[] targetArray, int dataSize,
            String methodName) throws IOException {
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(dataSize, sender.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        int count = 0;
        int total = 0;
        long beginTime = System.currentTimeMillis();
        while (total < dataSize && (count = receiver.read(targetBuf)) != -1) {
            total = total + count;
            if (System.currentTimeMillis() - beginTime > 3000){
                break;
            }
        }
        assertEquals(dataSize, total);
        assertEquals(targetBuf.position(), total);
        targetBuf.flip();
        targetArray = targetBuf.array();
        for (int i = 0; i < targetArray.length; i++) {
            assertEquals(targetArray[i], (byte) i);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadWrite_Block_64K() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_64KB];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        try {
            channel1.write(sourceBuf);
            fail("Should throw IOException");
        } catch (IOException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_DifferentAddr() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr1);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(CAPACITY_NORMAL, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        closeBlockedReaderChannel2(targetBuf);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_WriterNotBind() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(CAPACITY_NORMAL, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        closeBlockedReaderChannel2(targetBuf);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_WriterBindLater() throws Exception {
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    byte[] sourceArray = new byte[CAPACITY_NORMAL];
                    for (int i = 0; i < sourceArray.length; i++) {
                        sourceArray[i] = (byte) i;
                    }
                    channel1.socket().bind(localAddr2);
                    channel1.connect(localAddr1);
                    ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
                    assertEquals(CAPACITY_NORMAL, channel1.write(sourceBuf));
                } catch (Exception e) {
                }
            }
        }.start();
        int count = 0;
        int total = 0;
        long beginTime = System.currentTimeMillis();
        while (total < CAPACITY_NORMAL && (count = channel2.read(targetBuf)) != -1) {
            total = total + count;
            if (System.currentTimeMillis() - beginTime > 3000){
                break;
            }
        }
        assertEquals(CAPACITY_NORMAL, total);
        assertEquals(targetBuf.position(), total);
        targetBuf.flip();
        targetArray = targetBuf.array();
        for (int i = 0; i < targetArray.length; i++) {
            assertEquals(targetArray[i], (byte) i);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_Block_ReaderNotBind() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(CAPACITY_NORMAL, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        closeBlockedReaderChannel2(targetBuf);
    }
    private void closeBlockedReaderChannel2(ByteBuffer targetBuf)
            throws IOException {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel2.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            assertTrue(this.channel2.isBlocking());
            this.channel2.read(targetBuf);
            fail("Should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_Normal() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        readWriteReadData(this.channel1, sourceArray, this.channel2,
                targetArray, CAPACITY_NORMAL, "testReadWrite_NonBlock_Normal");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_8KB() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_1KB * 8];
        byte[] targetArray = new byte[CAPACITY_1KB * 8];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        readWriteReadData(this.channel1, sourceArray, this.channel2,
                targetArray, 8 * CAPACITY_1KB, "testReadWrite_NonBlock_8KB");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_DifferentAddr() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr1);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(CAPACITY_NORMAL, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        assertEquals(0, this.channel2.read(targetBuf));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_Empty() throws Exception {
        byte[] sourceArray = "".getBytes();
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(0, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        assertEquals(0, this.channel2.read(targetBuf));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_WriterNotBind() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(CAPACITY_NORMAL, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        assertEquals(0, this.channel2.read(targetBuf));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_Zero() throws Exception {
        byte[] sourceArray = new byte[0];
        byte[] targetArray = new byte[0];
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(0, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        assertEquals(0, this.channel2.read(targetBuf));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "read",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "write",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testReadWrite_NonBlock_ReaderNotBind() throws Exception {
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        byte[] targetArray = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < sourceArray.length; i++) {
            sourceArray[i] = (byte) i;
        }
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        assertEquals(CAPACITY_NORMAL, this.channel1.write(sourceBuf));
        ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);
        assertEquals(0, this.channel2.read(targetBuf));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void testWriteByteBuffer_Positioned() throws Exception {
        int postion = 16;
        DatagramChannel dc = DatagramChannel.open();
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        dc.connect(localAddr1);
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        sourceBuf.position(postion);
        assertEquals(CAPACITY_NORMAL - postion, dc.write(sourceBuf));
    }
    public void disabled_testWriteByteBuffer_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.send(targetBuf, localAddr1);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_testWriteByteBuffer_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends  Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
                    channel1.send(targetBuf, localAddr1);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_PositonNotZero()
            throws Exception {
        int CAPACITY_NORMAL = 256;
        int postion = 16;
        DatagramChannel dc = DatagramChannel.open();
        byte[] sourceArray = new byte[CAPACITY_NORMAL];
        ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
        sourceBuf.position(postion);
        int ret = dc.send(sourceBuf, localAddr1);
        assertEquals(CAPACITY_NORMAL - postion, ret);
        assertEquals(CAPACITY_NORMAL, sourceBuf.position());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer2() throws Exception {
        channel2.socket().bind(localAddr1);
        channel1.socket().bind(localAddr2);
        channel1.connect(localAddr1);
        channel2.connect(localAddr2);
        channel2.write(ByteBuffer.allocate(CAPACITY_NORMAL));
        ByteBuffer readBuf = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        channel1.configureBlocking(true);
        assertEquals(CAPACITY_NORMAL, channel1.read(readBuf));
    }
    public void disabled_testReadByteBuffer_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.read(targetBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer targetBuf = ByteBuffer.wrap(new byte[2]);
                    channel1.read(targetBuf);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )
    public void testReadByteBufferArray2() throws Exception {
        channel2.socket().bind(localAddr1);
        channel1.socket().bind(localAddr2);
        channel1.connect(localAddr1);
        channel2.connect(localAddr2);
        channel2.write(ByteBuffer.allocate(CAPACITY_NORMAL));
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        channel1.configureBlocking(true);
        assertEquals(CAPACITY_NORMAL, channel1.read(readBuf));
    }
    public void disabled_testReadByteBufferArray_Block_close() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        ByteBuffer[] targetBuf = new ByteBuffer[2];
        targetBuf[0] = ByteBuffer.wrap(new byte[2]);
        targetBuf[1] = ByteBuffer.wrap(new byte[2]);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    channel1.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            this.channel1.read(targetBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_testReadByteBufferArray_Block_interrupt() throws Exception {
        this.channel1.socket().bind(localAddr2);
        this.channel1.connect(localAddr1);
        this.channel2.socket().bind(localAddr1);
        this.channel2.connect(localAddr2);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    ByteBuffer[] targetBuf = new ByteBuffer[2];
                    targetBuf[0] = ByteBuffer.wrap(new byte[2]);
                    targetBuf[1] = ByteBuffer.wrap(new byte[2]);
                    channel1.read(targetBuf);
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (IOException e) {
                    errMsg = "Unexcted Exception was thrown: " + e.getClass() +
                            ": " + e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
        }
        thread.join(TIME_UNIT);
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void testReadByteBufferArrayII2() throws Exception {
        channel2.socket().bind(localAddr1);
        channel1.socket().bind(localAddr2);
        channel1.connect(localAddr1);
        channel2.connect(localAddr2);
        channel2.write(ByteBuffer.allocate(CAPACITY_NORMAL));
        ByteBuffer[] readBuf = new ByteBuffer[2];
        readBuf[0] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        channel1.configureBlocking(true);
        assertEquals(CAPACITY_NORMAL, channel1.read(readBuf,0,2));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_closed_nullBuf() throws Exception {
        ByteBuffer c = null;
        DatagramChannel channel = DatagramChannel.open();
        channel.close();
        try{
            channel.read(c);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e){
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_NotConnected_nullBuf() throws Exception {
        ByteBuffer c = null;
        DatagramChannel channel = DatagramChannel.open();
        try{
            channel.read(c);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e){
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer.class}
    )
    public void testReadByteBuffer_readOnlyBuf() throws Exception {
        ByteBuffer c = ByteBuffer.allocate(1);
        DatagramChannel channel = DatagramChannel.open();
        try{
            channel.read(c.asReadOnlyBuffer());
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e){
        }
        channel.connect(localAddr1);
        try{
            channel.read(c.asReadOnlyBuffer());
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e){
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "send",
        args = {java.nio.ByteBuffer.class, java.net.SocketAddress.class}
    )
    public void testSend_Closed() throws IOException{
        channel1.close();
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        try {
            channel1.send(buf, localAddr1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            channel1.send(null,localAddr1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            channel1.send(buf, null);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            channel1.send(null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void testSocket_NonBlock_IllegalBlockingModeException() throws Exception {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        DatagramSocket socket = channel.socket();
        try {
            socket.send(null);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        try {
            socket.receive(null);
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
    }
}
