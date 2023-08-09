@TestTargetClass(java.nio.ByteBuffer.class)
public class DuplicateHeapByteBufferTest extends HeapByteBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.duplicate();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
