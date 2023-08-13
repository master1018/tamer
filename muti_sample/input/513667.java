@TestTargetClass(java.nio.LongBuffer.class)
public class ReadOnlyWrappedLongBufferTest extends ReadOnlyLongBufferTest{
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = LongBuffer.wrap(new long[BUFFER_LENGTH]);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
}
