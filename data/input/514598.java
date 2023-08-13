public class CharsetDecoderTest extends junit.framework.TestCase {
    private static final String CHARSET = "UTF-16";
    private static final String SAMPLE_STRING = "Android";
    public void test_ByteArray_decode_no_offset() throws Exception {
        CharsetDecoder decoder = getCharsetDecoderUnderTest();
        byte[] arr = getEncodedByteArrayFixture();
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, 0, arr.length).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        assertFalse(coderResult.toString(), coderResult.isError());
        decoder.flush(outBuffer);
        outBuffer.flip();
        assertEquals(SAMPLE_STRING, outBuffer.toString().trim());
    }
    public void test_ByteArray_decode_with_offset() throws Exception {
        CharsetDecoder decoder = getCharsetDecoderUnderTest();
        byte[] arr = getEncodedByteArrayFixture();
        arr = prependByteToByteArray(arr, new Integer(1).byteValue());
        int offset = 1;
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, offset, arr.length - offset).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length - offset);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        assertFalse(coderResult.toString(), coderResult.isError());
        decoder.flush(outBuffer);
        outBuffer.flip();
        assertEquals(SAMPLE_STRING, outBuffer.toString().trim());
    }
    public void test_ByteArray_decode_with_offset_using_facade_method() throws Exception {
        CharsetDecoder decoder = getCharsetDecoderUnderTest();
        byte[] arr = getEncodedByteArrayFixture();
        arr = prependByteToByteArray(arr, new Integer(1).byteValue());
        int offset = 1;
        CharBuffer outBuffer = decoder.decode(ByteBuffer.wrap(arr, offset, arr.length - offset));
        assertEquals(SAMPLE_STRING, outBuffer.toString().trim());
    }
    private static byte[] prependByteToByteArray(byte[] arr, byte b) {
        byte[] result = new byte[arr.length + 1];
        result[0] = b;
        System.arraycopy(arr, 0, result, 1, arr.length);
        return result;
    }
    private static CharsetDecoder getCharsetDecoderUnderTest() {
        return Charset.forName(CHARSET).newDecoder();
    }
    private byte[] getEncodedByteArrayFixture() throws CharacterCodingException {
        CharsetEncoder encoder = Charset.forName(CHARSET).newEncoder();
        return encoder.encode(CharBuffer.wrap(SAMPLE_STRING)).array();
    }
}
