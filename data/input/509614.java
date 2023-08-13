@TestTargetClass(java.nio.IntBuffer.class)
public class ReadOnlyWrappedIntBufferTest extends ReadOnlyIntBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = IntBuffer.wrap(new int[BUFFER_LENGTH]);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
}
