@TestTargetClass(java.nio.CharBuffer.class)
public class ReadOnlyWrappedCharBufferTest1 extends ReadOnlyCharBufferTest {
    protected void setUp() throws Exception {
        capacity = BUFFER_LENGTH;
        buf = CharBuffer.wrap(new char[BUFFER_LENGTH]);
        loadTestData1(buf);
        buf = buf.asReadOnlyBuffer();
        baseBuf = buf;
    }
    protected void tearDown() throws Exception {
        buf = null;
        baseBuf = null;
    }
}
