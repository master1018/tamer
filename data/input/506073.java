@TestTargetClass(java.nio.charset.CharsetDecoder.class)
public class UTFCharsetDecoderTest extends AbstractCharsetDecoderTestCase {
    protected void setUp() throws Exception {
        cs = Charset.forName("utf-8");
        unibytes = new byte[] { 32, 98, 117, 102, 102, 101, 114 };
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        return null;
    }
    ByteBuffer getMalformByteBuffer() throws UnsupportedEncodingException {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put((byte) 0xd8);
        buffer.put(unibytes);
        buffer.flip();
        return buffer;
    }
    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
}
