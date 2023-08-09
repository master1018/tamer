@TestTargetClass(java.nio.charset.CharsetDecoder.class)
public class UTF16CharsetDecoderTest extends AbstractCharsetDecoderTestCase {
    boolean bigEndian = true;
    protected void setUp() throws Exception {
        cs = Charset.forName("utf-16");
        unibytes = new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0,
                114, 0 };
        bom = "\ufeff";
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    byte[] getUnibytes() {
        if (bigEndian) {
            return new byte[] { -1, -2, 32, 0, 98, 0, 117, 0, 102, 0, 102, 0,
                    101, 0, 114, 0 };
        } else {
            unibytes = new byte[] { 0, 32, 0, 98, 0, 117, 0, 102, 0, 102, 0,
                    101, 0, 114 };
            return new byte[] { -2, -1, 0, 32, 0, 98, 0, 117, 0, 102, 0, 102,
                    0, 101, 0, 114 };
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed.",
        method = "decode",
        args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
    )
    public void testMultiStepDecode() throws CharacterCodingException {
        if (!cs.name().equals("mock")) {
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            CharBuffer out = CharBuffer.allocate(10);
            assertTrue(decoder.decode(
                    ByteBuffer.wrap(new byte[] { -1, -2, 32, 0, 98 }), out,
                    true).isMalformed());
            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertSame(CoderResult.UNDERFLOW, decoder.decode(ByteBuffer
                    .wrap(new byte[] { -1, -2, 32, 0 }), out, false));
            assertTrue(decoder.decode(ByteBuffer.wrap(new byte[] { 98 }), out,
                    true).isMalformed());
            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertSame(CoderResult.UNDERFLOW, decoder.decode(ByteBuffer
                    .wrap(new byte[] { -1, -2, 32, 0, 98 }), out, false));
            assertFalse(decoder
                    .decode(ByteBuffer.wrap(new byte[] {}), out, true)
                    .isMalformed());
            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertFalse(decoder.decode(
                    ByteBuffer.wrap(new byte[] { -1, -2, 32, 0, 98, 0 }), out,
                    true).isError());
            decoder.flush(out);
            decoder.reset();
            out.clear();
            assertSame(CoderResult.UNDERFLOW, decoder.decode(ByteBuffer
                    .wrap(new byte[] { -1, -2, 32, 0, 98 }), out, false));
            assertTrue(decoder.decode(ByteBuffer.wrap(new byte[] { 0 }), out,
                    true).isMalformed());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Implementation in CharsetDecoderTest.implTestDecodeByteBufferCharBufferboolean & CharsetDecoderTest.implTestDecodeByteBuffer. Exceptions cheching missed.",
            method = "decode",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Implementation in CharsetDecoderTest.implTestDecodeByteBufferCharBufferboolean & CharsetDecoderTest.implTestDecodeByteBuffer. Exceptions cheching missed.",
            method = "decode",
            args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
        )
    })
    public void testLittleEndian() throws CharacterCodingException,
            UnsupportedEncodingException {
        bigEndian = false;
        implTestDecodeByteBufferCharBufferboolean();
        decoder.reset();
        implTestDecodeByteBuffer();
        bigEndian = true;
    }
    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        return null;
    }
    ByteBuffer getMalformByteBuffer() throws UnsupportedEncodingException {
        return null;
    }
    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
}
