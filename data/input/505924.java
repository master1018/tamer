@TestTargetClass(java.nio.charset.CharsetEncoder.class)
public class GBCharsetEncoderTest extends AbstractCharsetEncoderTestCase {
    protected void setUp() throws Exception {
        cs = Charset.forName("gb18030");;
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
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
        assertTrue(encoder.canEncode(""));
        assertTrue(encoder.canEncode("\ud800\udc00"));
        assertFalse(encoder.canEncode("\ud800\udb00"));
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
        assertEquals(4.0, encoder.maxBytesPerChar(), 0.0);
        assertEquals(2.5, encoder.averageBytesPerChar(), 0.0);
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
        return new byte[] { (byte) 0xd8, (byte) 0x00 };
    }
}
