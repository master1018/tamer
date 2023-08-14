@TestTargetClass(java.nio.FloatBuffer.class)
public class ReadOnlyWrappedFloatBufferTest extends ReadOnlyFloatBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = FloatBuffer.wrap(new float[BUFFER_LENGTH]);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
}
