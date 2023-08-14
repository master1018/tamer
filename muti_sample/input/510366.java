@TestTargetClass(java.nio.DoubleBuffer.class)
public class ReadOnlyWrappedDoubleBufferTest extends ReadOnlyDoubleBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = DoubleBuffer.wrap(new double[BUFFER_LENGTH]);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf =null;
        baseBuf = null;
    }
}
