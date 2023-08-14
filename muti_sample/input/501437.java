@TestTargetClass(java.nio.ShortBuffer.class)
public class ReadOnlyWrappedShortBufferTest extends ReadOnlyShortBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = ShortBuffer.wrap(new short[BUFFER_LENGTH]);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
}
