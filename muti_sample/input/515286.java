@TestTargetClass(java.nio.ByteBuffer.class)
public class DuplicateDirectByteBufferTest extends DirectByteBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        buf = buf.duplicate();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
