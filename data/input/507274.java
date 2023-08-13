@TestTargetClass(java.nio.charset.CharsetDecoder.class)
public class ASCCharsetDecoderTest extends AbstractCharsetDecoderTestCase {
    protected void setUp() throws Exception {
        cs = Charset.forName("ascii");
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
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) -1);
        buffer.put(unibytes);
        buffer.flip();
        return buffer;
    }
    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
}
