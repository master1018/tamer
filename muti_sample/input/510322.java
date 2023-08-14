@TestTargetClass(java.nio.charset.CharsetEncoder.class)
public class ISOCharsetEncoderTest extends AbstractCharsetEncoderTestCase {
    private static final Charset CS = Charset.forName("iso-8859-1");
    protected void setUp() throws Exception {
        cs = CS;
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    public void testCanEncodeCharSequence() {
        assertTrue(encoder.canEncode("\u0077"));
        assertFalse(encoder.canEncode("\uc2a3"));
        assertFalse(encoder.canEncode("\ud800\udc00"));
        try {
            encoder.canEncode(null);
        } catch (NullPointerException e) {
        }
        assertTrue(encoder.canEncode(""));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test. IllegalStateException checking missed.",
            method = "canEncode",
            args = {char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Regression test. IllegalStateException checking missed.",
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        )
    })
    public void testCanEncodeICUBug() {
        assertFalse(encoder.canEncode((char) '\ud800'));
        assertFalse(encoder.canEncode((String) "\ud800"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {char.class}
    )
    public void testCanEncodechar() throws CharacterCodingException {
        assertTrue(encoder.canEncode('\u0077'));
        assertFalse(encoder.canEncode('\uc2a3'));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "averageBytesPerChar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "maxBytesPerChar",
            args = {}
        )
    })
    public void testSpecificDefaultValue() {
        assertEquals(1, encoder.averageBytesPerChar(), 0.001);
        assertEquals(1, encoder.maxBytesPerChar(), 0.001);
    }
    CharBuffer getMalformedCharBuffer() {
        return CharBuffer.wrap("\ud800 buffer");
    }
    CharBuffer getUnmapCharBuffer() {
        return CharBuffer.wrap("\ud800\udc00 buffer");
    }
    CharBuffer getExceptionCharBuffer() {
        return null;
    }
    protected byte[] getIllegalByteArray() {
        return null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks also: flush & encode, but not covers exceptions.",
            method = "onMalformedInput",
            args = {java.nio.charset.CodingErrorAction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks also: flush & encode, but not covers exceptions.",
            method = "onUnmappableCharacter",
            args = {java.nio.charset.CodingErrorAction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks also: flush & encode, but not covers exceptions.",
            method = "reset",
            args = {}
        )
    })
    public void testMultiStepEncode() throws CharacterCodingException {
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            encoder.encode(CharBuffer.wrap("\ud800\udc00"));
            fail("should unmappable");
        } catch (UnmappableCharacterException e) {
        }
        encoder.reset();
        ByteBuffer out = ByteBuffer.allocate(10);
        assertTrue(encoder.encode(CharBuffer.wrap("\ud800"), out, true)
                .isMalformed());
        encoder.flush(out);
        encoder.reset();
        out = ByteBuffer.allocate(10);
        assertSame(CoderResult.UNDERFLOW, encoder.encode(CharBuffer
                .wrap("\ud800"), out, false));
        assertTrue(encoder.encode(CharBuffer.wrap("\udc00"), out, true)
                .isMalformed());
    }
}
