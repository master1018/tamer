@TestTargetClass(CharsetDecoder.class)
public class AbstractCharsetDecoderTestCase extends TestCase {
    Charset cs;
    protected static CharsetDecoder decoder;
    static final String unistr = " buffer";
    byte[] unibytes;
    String bom = "";
    protected void setUp() throws Exception {
        super.setUp();
        decoder = cs.newDecoder();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "charset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "detectedCharset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "isCharsetDetected",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "isAutoDetecting",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "malformedInputAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "unmappableCharacterAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "replacement",
            args = {}
        )
    })
    public void testDefaultValues() {
        assertSame(cs, decoder.charset());
        try {
            decoder.detectedCharset();
            fail("should unsupported");
        } catch (UnsupportedOperationException e) {
        }
        try {
            assertTrue(decoder.isCharsetDetected());
            fail("should unsupported");
        } catch (UnsupportedOperationException e) {
        }
        assertFalse(decoder.isAutoDetecting());
        assertSame(CodingErrorAction.REPORT, decoder.malformedInputAction());
        assertSame(CodingErrorAction.REPORT, decoder
                .unmappableCharacterAction());
        assertEquals(decoder.replacement(), "\ufffd");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "malformedInputAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "onMalformedInput",
            args = {java.nio.charset.CodingErrorAction.class}
        )
    })
    public void testOnMalformedInput() {
        assertSame(CodingErrorAction.REPORT, decoder.malformedInputAction());
        try {
            decoder.onMalformedInput(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        assertSame(CodingErrorAction.IGNORE, decoder.malformedInputAction());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "unmappableCharacterAction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "onUnmappableCharacter",
            args = {java.nio.charset.CodingErrorAction.class}
        )
    })
    public void testOnUnmappableCharacter() {
        assertSame(CodingErrorAction.REPORT, decoder
                .unmappableCharacterAction());
        try {
            decoder.onUnmappableCharacter(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
        assertSame(CodingErrorAction.IGNORE, decoder
                .unmappableCharacterAction());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "replaceWith",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "replacement",
            args = {}
        )
    })
    public void testReplaceWith() {
        try {
            decoder.replaceWith(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            decoder.replaceWith("");
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            decoder.replaceWith("testReplaceWith");
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }
        decoder.replaceWith("a");
        assertSame("a", decoder.replacement());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "decode",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testDecodeByteBuffer() throws CharacterCodingException {
        implTestDecodeByteBuffer();
    }
    void implTestDecodeByteBuffer() throws CharacterCodingException {
        try {
            decoder.decode(null);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        CharBuffer out = decoder.decode(ByteBuffer.allocate(0));
        assertCharBufferValue(out, "");
        ByteBuffer in = ByteBuffer.wrap(getUnibytes());
        out = decoder.decode(in);
        assertEquals(out.position(), 0);
        assertEquals(out.limit(), unistr.length());
        assertEquals(out.remaining(), unistr.length());
        assertEquals(new String(out.array(), 0, out.limit()), unistr);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "decode",
        args = {java.nio.ByteBuffer.class}
    )
    public void testDecodeByteBufferException()
            throws CharacterCodingException, UnsupportedEncodingException {
        CharBuffer out;
        ByteBuffer in;
        String replaceStr = decoder.replacement() + " buffer";
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        in = getMalformByteBuffer();
        if (in != null) {
            try {
                CharBuffer buffer = decoder.decode(in);
                assertTrue(buffer.remaining() > 0);
                fail("should throw MalformedInputException");
            } catch (MalformedInputException e) {
            }
            decoder.reset();
            in.rewind();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            out = decoder.decode(in);
            assertCharBufferValue(out, " buffer");
            decoder.reset();
            in.rewind();
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            out = decoder.decode(in);
            assertCharBufferValue(out, replaceStr);
        }
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        in = getUnmappedByteBuffer();
        if (in != null) {
            try {
                decoder.decode(in);
                fail("should throw UnmappableCharacterException");
            } catch (UnmappableCharacterException e) {
            }
            decoder.reset();
            in.rewind();
            decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            out = decoder.decode(in);
            assertCharBufferValue(out, " buffer");
            decoder.reset();
            in.rewind();
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            out = decoder.decode(in);
            assertCharBufferValue(out, replaceStr);
        }
        try {
            decoder.decode(getExceptionByteArray());
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "decode",
        args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
    )
    public void testDecodeByteBufferCharBufferboolean() {
        implTestDecodeByteBufferCharBufferboolean();
    }
    void implTestDecodeByteBufferCharBufferboolean() {
        byte[] gb = getUnibytes();
        ByteBuffer in = ByteBuffer.wrap(gb);
        CharBuffer out = CharBuffer.allocate(100);
        try {
            decoder.decode(null, out, true);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        try {
            decoder.decode(in, null, true);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        decoder.reset();
        in.rewind();
        out.rewind();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
        assertEquals(out.limit(), 100);
        assertEquals(out.position(), unistr.length());
        assertEquals(out.remaining(), 100 - unistr.length());
        assertEquals(out.capacity(), 100);
        assertCharBufferValue(out, unistr);
        decoder.flush(out);
        decoder.reset();
        in.rewind();
        out.clear();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        assertEquals(out.limit(), 100);
        assertEquals(out.position(), unistr.length());
        assertEquals(out.remaining(), 100 - unistr.length());
        assertEquals(out.capacity(), 100);
        assertCharBufferValue(out, unistr);
        decoder.reset();
        in.rewind();
        out.clear();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        in = ByteBuffer.wrap(unibytes);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        in.rewind();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
        assertEquals(out.limit(), 100);
        assertTrue(out.position() > 0);
        assertEquals(out.remaining(), out.capacity() - out.position());
        assertEquals(out.capacity(), 100);
        assertCharBufferValue(out, unistr + unistr + unistr);
        out = CharBuffer.allocate(4);
        decoder.reset();
        in = ByteBuffer.wrap(getUnibytes());
        out.rewind();
        assertSame(CoderResult.OVERFLOW, decoder.decode(in, out, false));
        assertEquals(new String(out.array()), unistr.substring(0, 4));
        out = CharBuffer.allocate(100);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        assertCharBufferValue(out, unistr.substring(4));
        in.rewind();
        out = CharBuffer.allocate(100);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
        assertCharBufferValue(out, bom + unistr);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "decode",
        args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
    )
    public void testDecodeCharBufferByteBufferbooleanExceptionTrue()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferbooleanException(true);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "decode",
        args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
    )
    public void testDecodeCharBufferByteBufferbooleanExceptionFalse()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferbooleanException(false);
    }
    void implTestDecodeCharBufferByteBufferbooleanException(boolean endOfInput)
            throws CharacterCodingException, UnsupportedEncodingException {
        CharBuffer out;
        ByteBuffer in;
        in = getUnmappedByteBuffer();
        out = CharBuffer.allocate(50);
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        if (null != in) {
            decoder.reset();
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            CoderResult result = decoder.decode(in, out, endOfInput);
            assertTrue(result.isUnmappable());
            decoder.reset();
            out.clear();
            in.rewind();
            decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out,
                    endOfInput));
            assertCharBufferValue(out, " buffer");
            decoder.reset();
            out.clear();
            in.rewind();
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out,
                    endOfInput));
            assertCharBufferValue(out, decoder.replacement() + " buffer");
        } else if (endOfInput) {
        }
        in = getMalformByteBuffer();
        out = CharBuffer.allocate(50);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        if (null != in) {
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            CoderResult result = decoder.decode(in, out, endOfInput);
            assertTrue(result.isMalformed());
            decoder.reset();
            out.clear();
            in.rewind();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out,
                    endOfInput));
            assertCharBufferValue(out, " buffer");
            decoder.reset();
            out.clear();
            in.rewind();
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out,
                    endOfInput));
            assertCharBufferValue(out, decoder.replacement() + " buffer");
        } else if (endOfInput) {
        }
        in = getExceptionByteArray();
        try {
            decoder.decode(in, out, endOfInput);
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
        }
    }
    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return ByteBuffer
                .wrap(new byte[] { 114, 117, 110, 116, 105, 109, 101 });
    }
    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        byte[] ba = new byte[] { 117, 110, 109, 97, 112, 32, 98, 117, 102, 102,
                101, 114 };
        return ByteBuffer.wrap(ba);
    }
    ByteBuffer getMalformByteBuffer() throws UnsupportedEncodingException {
        byte[] ba = new byte[] { 109, 97, 108, 102, 111, 114, 109, 32, 98, 117,
                102, 102, 101, 114 };
        return ByteBuffer.wrap(ba);
    }
    void assertCharBufferValue(CharBuffer out, String expected) {
        if (out.position() != 0) {
            out.flip();
        }
        assertEquals(new String(out.array(), 0, out.limit()), expected);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "flush",
        args = {java.nio.CharBuffer.class}
    )
    public void testFlush() throws CharacterCodingException {
        CharBuffer out = CharBuffer.allocate(10);
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 12, 12 });
        decoder.decode(in, out, true);
        assertSame(CoderResult.UNDERFLOW, decoder.flush(out));
        decoder.reset();
        decoder.decode((ByteBuffer) in.rewind(), (CharBuffer) out.rewind(),
                true);
        assertSame(CoderResult.UNDERFLOW, decoder
                .flush(CharBuffer.allocate(10)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "reset",
        args = {}
    )
    public void testResetIllegalState() throws CharacterCodingException {
        byte[] gb = getUnibytes();
        decoder.reset();
        decoder.decode(ByteBuffer.wrap(gb));
        decoder.reset();
        decoder.decode(ByteBuffer.wrap(gb), CharBuffer.allocate(3), false);
        decoder.reset();
        decoder.decode(ByteBuffer.wrap(gb), CharBuffer.allocate(3), true);
        decoder.reset();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "flush",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "reset",
            args = {}
        )
    })
    public void testFlushIllegalState() throws CharacterCodingException {
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 98, 98 });
        CharBuffer out = CharBuffer.allocate(5);
        decoder.reset();
        decoder.decode(in, out, true);
        out.rewind();
        CoderResult result = decoder.flush(out);
        assertSame(result, CoderResult.UNDERFLOW);
        try {
            decoder.flush(out);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        decoder.reset();
        decoder.decode(in, out, false);
        try {
            decoder.flush(out);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }
    byte[] getUnibytes() {
        return unibytes;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "decode",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "decode",
            args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
        )
    })
    public void testDecodeFacadeIllegalState() throws CharacterCodingException {
        byte[] gb = getUnibytes();
        ByteBuffer in = ByteBuffer.wrap(gb);
        decoder.decode(in);
        in.rewind();
        decoder.decode(in);
        in.rewind();
        decoder.reset();
        decoder.decode(ByteBuffer.wrap(gb), CharBuffer.allocate(30), true);
        decoder.decode(in);
        in.rewind();
        decoder.reset();
        decoder.decode(ByteBuffer.wrap(gb), CharBuffer.allocate(30), false);
        decoder.decode(in);
        in.rewind();
        decoder.reset();
        decoder.decode(ByteBuffer.wrap(gb), CharBuffer.allocate(30), true);
        decoder.flush(CharBuffer.allocate(10));
        decoder.decode(in);
        in.rewind();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "decode",
        args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
    )
    public void testDecodeTrueIllegalState() throws CharacterCodingException {
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 98, 98 });
        CharBuffer out = CharBuffer.allocate(100);
        decoder.decode(in, out, true);
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        in.rewind();
        decoder.decode(in, out, true);
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), false);
        in.rewind();
        decoder.decode(in, out, true);
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        decoder.flush(CharBuffer.allocate(10));
        in.rewind();
        try {
            decoder.decode(in, out, true);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        in.rewind();
        out.rewind();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "decode",
        args = {java.nio.ByteBuffer.class, java.nio.CharBuffer.class, boolean.class}
    )
    public void testDecodeFalseIllegalState() throws CharacterCodingException {
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 98, 98 });
        CharBuffer out = CharBuffer.allocate(5);
        decoder.decode(in, out, false);
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in);
        in.rewind();
        try {
            decoder.decode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        in.rewind();
        try {
            decoder.decode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), false);
        in.rewind();
        decoder.decode(in, out, false);
        in.rewind();
        out.rewind();
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        in.rewind();
        decoder.flush(CharBuffer.allocate(10));
        try {
            decoder.decode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
    }
}
