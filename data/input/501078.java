    value = java.nio.channels.Pipe.SinkChannel.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer[].class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "ClosedByInterruptException can not easily be tested",
            method = "write",
            args = {java.nio.ByteBuffer[].class}
        ) 
    }
)
public class SinkChannelTest extends TestCase {
    private static final int BUFFER_SIZE = 5;
    private static final String ISO8859_1 = "ISO8859-1";
    private Pipe pipe;
    private Pipe.SinkChannel sink;
    private Pipe.SourceChannel source;
    private ByteBuffer buffer;
    private ByteBuffer positionedBuffer;
    protected void setUp() throws Exception {
        super.setUp();
        pipe = Pipe.open();
        sink = pipe.sink();
        source = pipe.source();
        buffer = ByteBuffer.wrap("bytes".getBytes(ISO8859_1));
        positionedBuffer = ByteBuffer.wrap("12345bytes".getBytes(ISO8859_1));
        positionedBuffer.position(BUFFER_SIZE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "validOps",
        args = {}
    )
    public void test_validOps() {
        assertEquals(SelectionKey.OP_WRITE, sink.validOps());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )    
    public void test_write_LByteBuffer() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        int oldPosition;
        int currentPosition;
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);
            boolean isBlocking = sinkBlockingMode[i] && sourceBlockingMode[i];
            for (ByteBuffer buf : bufArray) {
                buf.mark();
                oldPosition = buf.position();
                sink.write(buf);
                ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
                int totalCount = 0;
                do {
                    int count = source.read(readBuf);
                    if (count > 0) {
                        totalCount += count;
                    }
                } while (totalCount != BUFFER_SIZE && !isBlocking);
                currentPosition = buf.position();
                assertEquals(BUFFER_SIZE, currentPosition - oldPosition);
                assertEquals("bytes", new String(readBuf.array(), ISO8859_1));
                buf.reset();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )    
    public void test_write_LByteBuffer_mutliThread() throws IOException,
            InterruptedException {
        final int THREAD_NUM = 20;
        final byte[] strbytes = "bytes".getBytes(ISO8859_1);
        Thread[] thread = new Thread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            thread[i] = new Thread() {
                public void run() {
                    try {
                        sink.write(ByteBuffer.wrap(strbytes));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        for (int i = 0; i < THREAD_NUM; i++) {
            thread[i].start();
        }
        for (int i = 0; i < THREAD_NUM; i++) {
            thread[i].join();
        }
        ByteBuffer readBuf = ByteBuffer.allocate(THREAD_NUM * BUFFER_SIZE);
        long totalCount = 0;
        do {
            long count = source.read(readBuf);
            if (count < 0) {
                break;
            }
            totalCount += count;
        } while (totalCount != (THREAD_NUM * BUFFER_SIZE));
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < THREAD_NUM; i++) {
            buf.append("bytes");
        }
        String readString = buf.toString();
        assertEquals(readString, new String(readBuf.array(), ISO8859_1));
    }
    public void disabled_test_read_LByteBuffer_mutliThread_close() throws Exception {
        ByteBuffer sourceBuf = ByteBuffer.allocate(1000);
        sink.configureBlocking(true);
        new Thread() {
            public void run() {
                try {
                    Thread.currentThread().sleep(500);
                    sink.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            sink.write(sourceBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
    public void disabled_test_read_LByteBuffer_mutliThread_interrupt() throws Exception {
        sink.configureBlocking(true);
        Thread thread = new Thread() {
            public void run() {
                try {
                    sink.write(ByteBuffer.allocate(10));
                    fail("should have thrown a ClosedByInterruptException.");
                } catch (ClosedByInterruptException e) {
                    return;
                } catch (IOException e) {
                    fail("should throw a ClosedByInterruptException but " +
                            "threw " + e.getClass() + ": " + e.getMessage());
                }
            }
        };
        thread.start();
        Thread.currentThread().sleep(500);
        thread.interrupt();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )    
    public void test_write_LByteBuffer_Exception() throws IOException {
        ByteBuffer nullBuf = null;
        try {
            sink.write(nullBuf);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )     
    @AndroidOnly("seems to run on newer RI versions")
    public void test_write_LByteBuffer_SourceClosed() throws IOException {
        source.close();
        int written = sink.write(buffer);
        assertEquals(BUFFER_SIZE, written);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException, NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )     
    public void test_write_LByteBuffer_SinkClosed() throws IOException {
        sink.close();
        try {
            sink.write(buffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        ByteBuffer nullBuf = null;
        try {
            sink.write(nullBuf);
            fail("should throw NullPointerException");
        } catch (ClosedChannelException e) {
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write_$LByteBuffer() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);
            buffer.position(0);
            positionedBuffer.position(BUFFER_SIZE);
            sink.write(bufArray);
            boolean isBlocking = sinkBlockingMode[i] && sourceBlockingMode[i];
            for (int j = 0; j < bufArray.length; ++j) {
                ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
                int totalCount = 0;
                do {
                    int count = source.read(readBuf);
                    if (count < 0) {
                        break;
                    }
                    totalCount += count;
                } while (totalCount != BUFFER_SIZE && !isBlocking);
                assertEquals("bytes", new String(readBuf.array(), ISO8859_1));
            }
            assertEquals(BUFFER_SIZE, buffer.position());
            assertEquals(10, positionedBuffer.position());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write_$LByteBuffer_Exception() throws IOException {
        ByteBuffer[] nullBufArrayRef = null;
        try {
            sink.write(nullBufArrayRef);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray = { buffer, nullBuf };
        try {
            sink.write(nullBufArray);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    @AndroidOnly("seems to run on newer RI versions")
    public void test_write_$LByteBuffer_SourceClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        source.close();
        long written = sink.write(bufArray);
        assertEquals(BUFFER_SIZE, written);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException, NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class}
    )
    public void test_write_$LByteBuffer_SinkClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        sink.close();
        try {
            sink.write(bufArray);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        ByteBuffer[] nullBufArrayRef = null;
        try {
            sink.write(nullBufArrayRef);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray = { nullBuf };
        try {
            sink.write(nullBufArray);
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
    public void test_write_$LByteBufferII() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);
            positionedBuffer.position(BUFFER_SIZE);
            sink.write(bufArray, 1, 1);
            boolean isBlocking = sinkBlockingMode[i] && sourceBlockingMode[i];
            ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
            int totalCount = 0;
            do {
                int count = source.read(readBuf);
                if (count < 0) {
                    break;
                }
                totalCount += count;
            } while (totalCount != BUFFER_SIZE && !isBlocking);
            assertEquals("bytes", new String(readBuf.array(), ISO8859_1));
            assertEquals(10, positionedBuffer.position());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException, IndexOutOfBoundsException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )        
    public void test_write_$LByteBufferII_Exception() throws IOException {
        ByteBuffer[] nullBufArrayRef = null;
        try {
            sink.write(nullBufArrayRef, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            sink.write(nullBufArrayRef, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray = { nullBuf };
        try {
            sink.write(nullBufArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            sink.write(nullBufArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer[] bufArray = { buffer, nullBuf };
        try {
            sink.write(bufArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )        
    @AndroidOnly("seems to run on newer RI versions")
    public void test_write_$LByteBufferII_SourceClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        source.close();
        long written = sink.write(bufArray, 0, 1);
        assertEquals(BUFFER_SIZE, written);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException, NullPointerException, IndexOutOfBoundsException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )        
    public void test_write_$LByteBufferII_SinkClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        sink.close();
        try {
            sink.write(bufArray, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        ByteBuffer[] nullBufArrayRef = null;
        try {
            sink.write(nullBufArrayRef, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            sink.write(nullBufArrayRef, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray = { nullBuf };
        try {
            sink.write(nullBufArray, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            sink.write(nullBufArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer[] bufArray2 = { buffer, nullBuf };
        try {
            sink.write(bufArray2, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray2, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray2, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray2, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sink.write(bufArray2, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "close",
            args = {}
        ),@TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "isOpen",
            args = {}
        )
    })
    public void test_close() throws IOException {
        assertTrue(sink.isOpen());
        sink.close();
        assertFalse(sink.isOpen());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SinkChannel",
        args = {java.nio.channels.spi.SelectorProvider.class}
    )
    public void testConstructor() throws IOException {
        SinkChannel channel =
                SelectorProvider.provider().openPipe().sink();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(),channel.provider());
        channel = Pipe.open().sink();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(),channel.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that NullPointerException is thrown if write" +
                "method is called for closed channel.",
        method = "write",
        args = {java.nio.ByteBuffer.class}
    )
    public void test_socketChannel_closed() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(),49999));
        SocketChannel sc = SocketChannel.open();
        ByteBuffer buf = null;  
        try{
            sc.write(buf);
            fail("should throw NPE");
        }catch (NullPointerException e){
        }
        sc.connect(new InetSocketAddress(InetAddress.getLocalHost(),49999));
        SocketChannel sock = ssc.accept();              
        ssc.close();
        sc.close();
        try{
            sc.write(buf);
            fail("should throw NPE");
        }catch (NullPointerException e){
        }
        sock.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "write",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )
    public void test_socketChannel_empty() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(),49999));
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(InetAddress.getLocalHost(),49999));
        SocketChannel sock = ssc.accept();
        ByteBuffer[] buf = {ByteBuffer.allocate(10),null};                
        try{
            sc.write(buf,0,2);
            fail("should throw NPE");
        }catch (NullPointerException e){
        }
        ssc.close();
        sc.close();
        ByteBuffer target = ByteBuffer.allocate(10);
        assertEquals(-1, sock.read(target));
    }
}
