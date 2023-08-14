@TestTargetClass(java.nio.ByteBuffer.class)
public class SliceHeapByteBufferTest extends HeapByteBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        capacity = BUFFER_LENGTH - 2;
        buf.position(1).limit(BUFFER_LENGTH - 1);
        buf = buf.slice();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
