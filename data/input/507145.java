@TestTargetClass(ServerSocketChannel.class)
public class ServerSocketChannelTest extends TestCase {
    private static final int CAPACITY_NORMAL = 200;
    private static final int CAPACITY_64KB = 65536;
    private static final int TIME_UNIT = 200;
    private InetSocketAddress localAddr1;
    private ServerSocketChannel serverChannel;
    private SocketChannel clientChannel;
    protected void setUp() throws Exception {
        super.setUp();
        this.localAddr1 = new InetSocketAddress(
                "127.0.0.1", Support_PortManager 
                        .getNextPort());
        this.serverChannel = ServerSocketChannel.open();
        this.clientChannel = SocketChannel.open();
    }
    protected void tearDown() throws Exception {
        if (null != this.serverChannel) {
            try {
                this.serverChannel.close();
            } catch (Exception e) {
            }
        }
        if (null != this.clientChannel) {
            try {
                this.clientChannel.close();
            } catch (Exception e) {
            }
        }
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ServerSocketChannel",
        args = {java.nio.channels.spi.SelectorProvider.class}
    )
    public void testConstructor() throws IOException {
        ServerSocketChannel channel = 
                SelectorProvider.provider().openServerSocketChannel();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(),channel.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "validOps",
        args = {}
    )
    public void test_validOps() {
        MockServerSocketChannel testMSChnlnull = new MockServerSocketChannel(
                null);
        MockServerSocketChannel testMSChnl = new MockServerSocketChannel(
                SelectorProvider.provider());
        assertEquals(SelectionKey.OP_ACCEPT, this.serverChannel.validOps());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnl.validOps());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnlnull.validOps());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies in setUp method.",
        method = "open",
        args = {}
    )
    public void test_open() {
        MockServerSocketChannel testMSChnl = new MockServerSocketChannel(null);
        MockServerSocketChannel testMSChnlnotnull = new MockServerSocketChannel(
                SelectorProvider.provider());
        assertEquals(SelectionKey.OP_ACCEPT, testMSChnlnotnull.validOps());
        assertNull(testMSChnl.provider());
        assertNotNull(testMSChnlnotnull.provider());
        assertEquals(testMSChnlnotnull.provider(), this.serverChannel
                .provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isOpen",
        args = {}
    )
    public void testIsOpen() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "socket",
            args = {}
        ),@TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "isOpen",
            args = {}
        )
    })
    public void test_socket_Block_BeforeClose() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket s1 = this.serverChannel.socket();
        assertFalse(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
        s1.close();
        assertFalse(this.serverChannel.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void test_socket_NonBlock_BeforeClose() throws Exception {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.configureBlocking(false);
        ServerSocket s1 = this.serverChannel.socket();
        assertFalse(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
        s1.close();
        assertFalse(this.serverChannel.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void test_socket_Block_Closed() throws Exception {
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket s1 = this.serverChannel.socket();
        assertTrue(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "socket",
        args = {}
    )
    public void test_socket_NonBlock_Closed() throws Exception {
        this.serverChannel.configureBlocking(false);
        this.serverChannel.close();
        assertFalse(this.serverChannel.isBlocking());
        assertFalse(this.serverChannel.isOpen());
        ServerSocket s1 = this.serverChannel.socket();
        assertTrue(s1.isClosed());
        assertSocketNotAccepted(s1);
        ServerSocket s2 = this.serverChannel.socket();
        assertSame(s1, s2);
    }
    private void assertSocketNotAccepted(ServerSocket s) throws IOException {
        assertFalse(s.isBound());
        assertNull(s.getInetAddress());
        assertEquals(-1, s.getLocalPort());
        assertNull(s.getLocalSocketAddress());
        assertEquals(0, s.getSoTimeout());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of ServerSocketChannel.",
            method = "validOps",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of ServerSocketChannel.",
            method = "provider",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of ServerSocketChannel.",
            method = "isRegistered",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies default status of ServerSocketChannel.",
            method = "isBlocking",
            args = {}
        )
    })
    public void testChannelBasicStatus() {
        ServerSocket gotSocket = this.serverChannel.socket();
        assertFalse(gotSocket.isClosed());
        assertTrue(this.serverChannel.isBlocking());
        assertFalse(this.serverChannel.isRegistered());
        assertEquals(SelectionKey.OP_ACCEPT, this.serverChannel.validOps());
        assertEquals(SelectorProvider.provider(), this.serverChannel.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NotYetBoundException.",
        method = "accept",
        args = {}
    )
    public void test_accept_Block_NotYetBound() throws IOException {
        assertTrue(this.serverChannel.isOpen());
        assertTrue(this.serverChannel.isBlocking());
        try {
            this.serverChannel.accept();
            fail("Should throw NotYetBoundException"); 
        } catch (NotYetBoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NotYetBoundException.",
        method = "accept",
        args = {}
    )
    public void test_accept_NonBlock_NotYetBound() throws IOException {
        assertTrue(this.serverChannel.isOpen());
        this.serverChannel.configureBlocking(false);
        try {
            this.serverChannel.accept();
            fail("Should throw NotYetBoundException"); 
        } catch (NotYetBoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException.",
        method = "accept",
        args = {}
    )
    public void test_accept_ClosedChannel() throws Exception {
        this.serverChannel.close();
        assertFalse(this.serverChannel.isOpen());
        try {
            this.serverChannel.accept();
            fail("Should throw ClosedChannelException"); 
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies AsynchronousCloseException.",
        method = "accept",
        args = {}
    )
    public void test_accept_Block_NoConnect_close() throws IOException {
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket gotSocket = this.serverChannel.socket();
        gotSocket.bind(localAddr1);         
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(TIME_UNIT);
                    ServerSocketChannelTest.this.serverChannel.close();
                } catch (Exception e) {
                    fail("Fail to close the server channel because of"
                            + e.getClass().getName());
                }
            }
        }.start();
        try {
            this.serverChannel.accept();
            fail("Should throw a AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedByInterruptException.",
        method = "accept",
        args = {}
    )
    public void test_accept_Block_NoConnect_interrupt() throws IOException {
        assertTrue(this.serverChannel.isBlocking());
        ServerSocket gotSocket = this.serverChannel.socket();
        gotSocket.bind(localAddr1);
        class MyThread extends Thread {
            public String errMsg = null;
            public void run() {
                try {
                    serverChannel.accept();
                    errMsg = "should throw ClosedByInterruptException";
                } catch (ClosedByInterruptException e) {
                } catch (Exception e) {
                    errMsg = "caught wrong Exception: " + e.getClass() + ": " + 
                            e.getMessage();
                }
            }
        }
        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.currentThread().sleep(TIME_UNIT);
            thread.interrupt();
        } catch (InterruptedException e) {
            fail("Should not throw a InterruptedException");
        }
        if (thread.errMsg != null) {
            fail(thread.errMsg);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that accept() returns null if the channel is in non-blocking mode and no connection is available to be accepted.",
        method = "accept",
        args = {}
    )
    public void test_accept_NonBlock_NoConnect() throws IOException {
        ServerSocket gotSocket = this.serverChannel.socket();
        gotSocket.bind(localAddr1); 
        this.serverChannel.configureBlocking(false);
        assertNull(this.serverChannel.accept());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )
    public void test_accept_socket_read_Block() throws IOException {
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            buf.put((byte) i);
        }
        clientChannel.connect(localAddr1); 
        Socket serverSocket = serverChannel.accept().socket();
        InputStream in = serverSocket.getInputStream();
        buf.flip();
        clientChannel.write(buf);
        clientChannel.close();
        assertReadResult(in,CAPACITY_NORMAL);
    }
    private void assertReadResult(InputStream in, int size) throws IOException{
        byte[] readContent = new byte[size + 1];
        int count = 0;
        int total = 0;
        while ((count = in.read(readContent, total, size + 1 - total)) != -1) {
            total = total + count;
        }
        assertEquals(size, total);
        for (int i = 0; i < size; i++) {
            assertEquals((byte) i, readContent[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )    
    public void test_accept_socket_read_NonBlock() throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(localAddr1); 
        Socket serverSocket = serverChannel.accept().socket();
        InputStream in = serverSocket.getInputStream();
        clientChannel.write(buf);
        clientChannel.close();
        assertReadResult(in,CAPACITY_NORMAL);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )
    public void test_accept_socket_write_Block() throws IOException {
        assertTrue(serverChannel.isBlocking());
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }       
        clientChannel.connect(localAddr1); 
        Socket socket = serverChannel.accept().socket();
        OutputStream out = socket.getOutputStream();
        out.write(writeContent);
        out.flush();
        socket.close();    
        assertWriteResult(CAPACITY_NORMAL);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )
    public void test_accept_socket_write_NonBlock() throws Exception {
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1);
        Socket clientSocket = serverChannel.accept().socket();
        OutputStream out = clientSocket.getOutputStream();
        out.write(writeContent);
        clientSocket.close();  
        assertWriteResult(CAPACITY_NORMAL);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )    
    @BrokenTest("Sporadic timeouts in CTS, but not in CoreTestRunner")
    public void test_accept_socket_read_Block_RWLargeData() throws IOException {
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_64KB);
        for (int i = 0; i < CAPACITY_64KB; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(localAddr1);
        clientChannel.write(buf);
        clientChannel.close();
        Socket socket = serverChannel.accept().socket();
        InputStream in = socket.getInputStream();
        assertReadResult(in,CAPACITY_64KB);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )    
    @BrokenTest("Sporadic timeouts in CTS, but not in CoreTestRunner")
    public void test_accept_socket_read_NonBlock_RWLargeData()
            throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(localAddr1);
        ByteBuffer buf = ByteBuffer.allocate(CAPACITY_64KB);
        for (int i = 0; i < CAPACITY_64KB; i++) {
            buf.put((byte) i);
        }
        buf.flip();
        clientChannel.connect(localAddr1);
        clientChannel.write(buf);
        clientChannel.close();
        Socket socket = serverChannel.accept().socket();
        InputStream in = socket.getInputStream();
        assertReadResult(in,CAPACITY_64KB);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )
    @BrokenTest("Sporadic timeouts in CTS, but not in CoreTestRunner")
    public void test_accept_socket_write_NonBlock_RWLargeData()
            throws Exception {
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_64KB];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1); 
        Socket socket = serverChannel.accept().socket();
        OutputStream out = socket.getOutputStream();
        out.write(writeContent);
        socket.close();
        assertWriteResult(CAPACITY_64KB);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )
    @BrokenTest("Sporadic timeouts in CTS, but not in CoreTestRunner")
    public void test_accept_socket_write_Block_RWLargeData() throws Exception {
        serverChannel.socket().bind(localAddr1);
        byte[] writeContent = new byte[CAPACITY_64KB];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        clientChannel.connect(localAddr1); 
        Socket socket = serverChannel.accept().socket();
        OutputStream out = socket.getOutputStream();
        out.write(writeContent);
        socket.close();
        assertWriteResult(CAPACITY_64KB);
    }
    private void assertWriteResult(int size) throws IOException{
        ByteBuffer buf = ByteBuffer.allocate(size + 1);
        int count = 0;
        int total = 0;
        long beginTime = System.currentTimeMillis();
        while ((count = clientChannel.read(buf)) != -1) {
            total = total + count;
            if (System.currentTimeMillis() - beginTime > 10000){
                break;
            }
        }
        assertEquals(total, size);
        buf.flip();
        for (int i = 0; i < count; i++) {
            assertEquals((byte) i, buf.get(i));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that accept method  returns null since there are no pending connections. Doesn't verify exceptions.",
        method = "accept",
        args = {}
    )
    public void test_accept_SOTIMEOUT() throws IOException {
        final int SO_TIMEOUT = 10;
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            ServerSocket ss = ssc.socket();
            ss.bind(localAddr1);
            ssc.configureBlocking(false);
            ss.setSoTimeout(SO_TIMEOUT);
            SocketChannel client = ssc.accept();
            assertNull(client);
            int soTimeout = ss.getSoTimeout();
            assertEquals(SO_TIMEOUT, soTimeout);
        } finally {
            ssc.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "accept",
        args = {}
    )
    @KnownFailure("http:
    public void test_accept_Security() throws IOException {
        this.clientChannel.configureBlocking(true);
        this.serverChannel.configureBlocking(true);
        SecurityManager sm = System.getSecurityManager();
        MockSecurityManager mockManager = new MockSecurityManager("127.0.0.1");
        System.setSecurityManager(mockManager);
        Thread t = new Thread() {
            public void run() {
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    clientChannel.connect(localAddr1);
                } catch (IOException e) {
                }
            }
        };
        t.start();
        try {
            ServerSocket ss = this.serverChannel.socket();
            ss.bind(localAddr1);
            this.serverChannel.accept();
        } finally {
            System.setSecurityManager(sm);
        }
        assertTrue(mockManager.checkAcceptCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test. Verifies IllegalBlockingModeException.",
        method = "socket",
        args = {}
    )    
    public void test_socket_accept_Blocking_NotBound() throws IOException {
        ServerSocket gotSocket = serverChannel.socket();
        serverChannel.configureBlocking(true);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }        
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }     
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test. Verifies IllegalBlockingModeException.",
        method = "socket",
        args = {}
    )    
    public void test_socket_accept_Nonblocking_NotBound() throws IOException {
        ServerSocket gotSocket = serverChannel.socket();
        serverChannel.configureBlocking(false);
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }        
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }     
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test. Verifies IllegalBlockingModeException, ClosedChannelException.",
        method = "socket",
        args = {}
    )
    public void test_socket_accept_Nonblocking_Bound() throws IOException {
        serverChannel.configureBlocking(false);
        ServerSocket gotSocket = serverChannel.socket();
        gotSocket.bind(localAddr1);         
        try {
            gotSocket.accept();
            fail("Should throw an IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw a ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test. Verifies ClosedChannelException.",
        method = "socket",
        args = {}
    )
    public void test_socket_accept_Blocking_Bound() throws IOException {
        serverChannel.configureBlocking(true);
        ServerSocket gotSocket = serverChannel.socket();
        gotSocket.bind(localAddr1);         
        serverChannel.close();
        try {
            gotSocket.accept();
            fail("Should throw a ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test. Verifies that returned socket returns correct local port.",
        method = "socket",
        args = {}
    )
    public void test_socket_getLocalPort() throws IOException {
        serverChannel.socket().bind(localAddr1);
        clientChannel.connect(localAddr1); 
        SocketChannel myChannel = serverChannel.accept();
        int port = myChannel.socket().getLocalPort();
        assertEquals(localAddr1.getPort(), port);
        myChannel.close();
        clientChannel.close();
        serverChannel.close();
    }
}
