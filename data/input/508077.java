@TestTargetClass(java.nio.charset.CharsetEncoder.class)
public class UTF16LECharsetEncoderTest extends AbstractCharsetEncoderTestCase {
    private static final Charset CS = Charset.forName("utf-16le");
    protected void setUp() throws Exception {
        cs = CS;
        specifiedReplacement = new byte[] { -3, -1 };
        unibytes = new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0,
                114, 0 };
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Empty constructor test.",
        method = "CharsetEncoder",
        args = {java.nio.charset.Charset.class, float.class, float.class}
    )
    public void testCharsetEncoderCharsetfloatfloat() {
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {char.class}
    )
    public void testCanEncodechar() throws CharacterCodingException {
        assertTrue(encoder.canEncode('\u0077'));
        assertTrue(encoder.canEncode('\uc2a3'));
        assertTrue(encoder.canEncode('\uc2c0'));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    public void testCanEncodeCharSequence() {
        assertTrue(encoder.canEncode("\u0077"));
        assertTrue(encoder.canEncode("\uc2a3"));
        assertTrue(encoder.canEncode(""));
        assertTrue(encoder.canEncode("\uc2c0"));
        assertTrue(encoder.canEncode("\ud800\udc00"));
        assertFalse(encoder.canEncode("\ud800\udb00"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test. IllegalStateException checking missed.",
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    public void testCanEncodeICUBug() {
        assertFalse(encoder.canEncode("\ud800"));
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
        assertEquals(2, encoder.averageBytesPerChar(), 0.001);
        assertEquals(2, encoder.maxBytesPerChar(), 0.001);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLegalReplacement",
        args = {byte[].class}
    )
    public void testIsLegalReplacementEmptyArray() {
        assertTrue(encoder.isLegalReplacement(new byte[0]));
    }
    CharBuffer getMalformedCharBuffer() {
        return CharBuffer.wrap("\ud800 buffer");
    }
    CharBuffer getUnmapCharBuffer() {
        return null;
    }
    CharBuffer getExceptionCharBuffer() {
        return null;
    }
    protected byte[] getIllegalByteArray() {
        return new byte[] { 0x00 };
    }
    protected byte[] getLegalByteArray() {
        return new byte[] { (byte) 0xd8, 0x00 };
    }
}
