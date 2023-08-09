@TestTargetClass(java.nio.ByteBuffer.class)
public class DuplicateWrappedByteBufferTest extends WrappedByteBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.duplicate();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
