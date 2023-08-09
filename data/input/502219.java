    value = java.nio.channels.Pipe.SourceChannel.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "AsynchronousCloseException can not easily be tested",
            method = "read",
            args = {java.nio.ByteBuffer[].class}
        ) 
    }
)
public class SourceChannelTest extends TestCase {
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
        assertEquals(SelectionKey.OP_READ, source.validOps());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )   
    public void test_read_LByteBuffer_DataAvailable() throws IOException {
        sink.write(ByteBuffer.allocate(1));
        int count = source.read(ByteBuffer.allocate(10));
        assertEquals(1, count);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )       
    public void test_read_LByteBuffer_Exception() throws IOException {
        ByteBuffer nullBuf = null;
        try {
            source.read(nullBuf);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )       
    public void test_read_LByteBuffer_SinkClosed() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        sink.write(buffer);
        sink.close();
        long count = source.read(readBuf);
        assertEquals(BUFFER_SIZE, count);
        count = source.read(readBuf);
        assertEquals(0, count);
        readBuf.position(0);
        count = source.read(readBuf);
        assertEquals(-1, count);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException, NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )   
    public void test_read_LByteBuffer_SourceClosed() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        source.close();
        try {
            source.read(readBuf);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readBuf.position(BUFFER_SIZE);
        try {
            source.read(readBuf);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        ByteBuffer nullBuf = null;
        try {
            source.read(nullBuf);
            fail("should throw NullPointerException");
        } catch (ClosedChannelException e) {
        } catch (NullPointerException e) {
        }
        ByteBuffer[] bufArray = null; 
        try {
            source.read(bufArray);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ByteBuffer[] nullBufArray = {nullBuf}; 
        try {
            source.read(nullBufArray);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )     
    public void test_read_$LByteBuffer() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            pipe = Pipe.open();
            sink = pipe.sink();
            source = pipe.source();
            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);
            buffer.position(0);
            positionedBuffer.position(BUFFER_SIZE);
            try {
                long writeCount = sink.write(bufArray);
                assertEquals(10, writeCount);
                sink.close();
                ByteBuffer[] readBufArray = { ByteBuffer.allocate(BUFFER_SIZE),
                        ByteBuffer.allocate(BUFFER_SIZE) };
                long totalCount = 0;
                do {
                    long count = source.read(readBufArray);
                    if (count < 0) {
                        break;
                    }
                    if (0 == count && BUFFER_SIZE == readBufArray[1].position()) {
                        break;
                    }
                    totalCount += count;
                } while (totalCount <= 10);
                for (ByteBuffer readBuf : readBufArray) {
                    assertEquals(BUFFER_SIZE, readBuf.position());
                    assertEquals("bytes",
                            new String(readBuf.array(), ISO8859_1));
                }
            } finally {
                sink.close();
                source.close();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )     
    public void test_read_$LByteBuffer_Exception() throws IOException {
        ByteBuffer[] nullBufArrayRef = null;
        try {
            source.read(nullBufArrayRef);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray1 = { nullBuf };
        try {
            source.read(nullBufArray1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ByteBuffer[] nullBufArray2 = { buffer, nullBuf };
        try {
            source.read(nullBufArray2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )     
    public void test_read_$LByteBuffer_SinkClosed() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBuffer[] readBufArray = { readBuf };
        sink.write(buffer);
        sink.close();
        long count = source.read(readBufArray);
        assertEquals(BUFFER_SIZE, count);
        count = source.read(readBufArray);
        assertEquals(0, count);
        readBuf.position(0);
        assertTrue(readBuf.hasRemaining());
        count = source.read(readBufArray);
        assertEquals(-1, count);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedChannelException, NullPointerException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )     
    public void test_read_$LByteBuffer_SourceClosed() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBuffer[] readBufArray = { readBuf };
        source.close();
        try {
            source.read(readBufArray);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readBuf.position(BUFFER_SIZE);
        try {
            source.read(readBufArray);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        ByteBuffer[] nullBufArrayRef = null;
        try {
            source.read(nullBufArrayRef);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray1 = { nullBuf };
        try {
            source.read(nullBufArray1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )     
    public void test_read_$LByteBufferII() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            Pipe pipe = Pipe.open();
            sink = pipe.sink();
            source = pipe.source();
            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);
            buffer.position(0);
            positionedBuffer.position(BUFFER_SIZE);
            try {
                sink.write(bufArray);
                sink.close();
                ByteBuffer[] readBufArray = { ByteBuffer.allocate(BUFFER_SIZE),
                        ByteBuffer.allocate(BUFFER_SIZE) };
                long totalCount = 0;
                do {
                    long count = source.read(readBufArray, 0, 2);
                    if (count < 0) {
                        break;
                    }
                    if (0 == count && BUFFER_SIZE == readBufArray[1].position()) {
                        break;
                    }
                    totalCount += count;
                } while (totalCount != 10);
                for (ByteBuffer readBuf : readBufArray) {
                    assertEquals(BUFFER_SIZE, readBuf.position());
                    assertEquals("bytes",
                            new String(readBuf.array(), ISO8859_1));
                }
            } finally {
                sink.close();
                source.close();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException, IndexOutOfBoundsException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )   
    public void test_read_$LByteBufferII_Exception() throws IOException {
        ByteBuffer[] nullBufArrayRef = null;
        try {
            source.read(nullBufArrayRef, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            source.read(nullBufArrayRef, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray1 = { nullBuf };
        try {
            source.read(nullBufArray1, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            source.read(nullBufArray1, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray1, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray1, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer[] nullBufArray2 = { buffer, nullBuf };
        try {
            source.read(nullBufArray1, 1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray2, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray2, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )   
    public void test_read_$LByteBufferII_SinkClosed() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBuffer[] readBufArray = { readBuf };
        sink.write(buffer);
        sink.close();
        long count = source.read(readBufArray, 0, 1);
        assertEquals(BUFFER_SIZE, count);
        count = source.read(readBufArray);
        assertEquals(0, count);
        readBuf.position(0);
        count = source.read(readBufArray, 0, 1);
        assertEquals(-1, count);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException, ClosedChannelException.",
        method = "read",
        args = {java.nio.ByteBuffer[].class, int.class, int.class}
    )   
    public void test_read_$LByteBufferII_SourceClosed() throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBuffer[] readBufArray = { readBuf };
        source.close();
        try {
            source.read(readBufArray, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        readBuf.position(BUFFER_SIZE);
        try {
            source.read(readBufArray, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        ByteBuffer[] nullBufArrayRef = null;
        try {
            source.read(nullBufArrayRef, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            source.read(nullBufArrayRef, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray1 = { nullBuf };
        try {
            source.read(nullBufArray1, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
        }
        try {
            source.read(nullBufArray1, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray1, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray1, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        ByteBuffer[] nullBufArray2 = { buffer, nullBuf };
        try {
            source.read(nullBufArray1, 1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray2, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            source.read(nullBufArray2, 0, 2);
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
        method = "SourceChannel",
        args = {java.nio.channels.spi.SelectorProvider.class}
    )
    public void testConstructor() throws IOException {
        SourceChannel channel =
                SelectorProvider.provider().openPipe().source();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(),channel.provider());
        channel = Pipe.open().source();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(),channel.provider());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClosedByInterruptException",
        method = "read",
        args = {java.nio.ByteBuffer[].class}
    )    
    public void test_read_LByteBuffer_mutliThread_interrupt() throws Exception {
        source.configureBlocking(true);
        Thread thread = new Thread() {
            public void run() {
                try {
                    source.read(ByteBuffer.allocate(10));
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
    public void disabled_test_read_LByteBuffer_mutliThread_close() throws Exception {
        ByteBuffer sourceBuf = ByteBuffer.allocate(1000);
        source.configureBlocking(true);
        new Thread() {
            public void run() {
                try {
                    Thread.currentThread().sleep(500);
                    source.close();
                } catch (Exception e) {
                }
            }
        }.start();
        try {
            source.read(sourceBuf);
            fail("should throw AsynchronousCloseException");
        } catch (AsynchronousCloseException e) {
        }
    }
}
