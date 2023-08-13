@TestTargetClass(java.nio.charset.CharsetDecoder.class)
public class GBCharsetDecoderTest extends AbstractCharsetDecoderTestCase {
    protected void setUp() throws Exception {
        cs = Charset.forName("gb18030");
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
        buffer.put(new byte[] { (byte) 0xd8, 0 });
        buffer.put(unibytes);
        buffer.flip();
        return buffer;
    }
    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
}
