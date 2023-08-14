@TestTargetClass(CharsetEncoder.class)
public class ASCIICharsetEncoderTest extends TestCase {
    private static final Charset cs = Charset.forName("ascii");
    private static final CharsetEncoder encoder = cs.newEncoder();
    private static final int MAXCODEPOINT = 0x7F; 
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
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
            notes = "IllegalStateException checking missed.",
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "IllegalStateException checking missed.",
            method = "canEncode",
            args = {char.class}
        )
    })
    public void testCanEncodeSurrogate () {
        assertFalse(encoder.canEncode('\ud800'));
        assertFalse(encoder.canEncode("\udc00"));
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
        assertEquals(1.0, encoder.averageBytesPerChar(), 0.0);
        assertEquals(1.0, encoder.maxBytesPerChar(), 0.0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "encode",
        args = {java.nio.CharBuffer.class}
    )
    public void testEncodeMapping() throws CharacterCodingException {
        encoder.reset();
        for (int i =0; i <= MAXCODEPOINT; i++) {
            char[] chars = Character.toChars(i);
            CharBuffer cb = CharBuffer.wrap(chars);
            ByteBuffer bb = encoder.encode(cb);
            assertEquals(i, bb.get(0));
        }
        CharBuffer cb = CharBuffer.wrap("\u0080");
        try {
            encoder.encode(cb);
        } catch (UnmappableCharacterException e) {
        }
        cb = CharBuffer.wrap("\ud800");
        try {
            encoder.encode(cb);
        } catch (MalformedInputException e) {
        }
        ByteBuffer bb = ByteBuffer.allocate(0x10);
        cb = CharBuffer.wrap("A");
        encoder.reset();
        encoder.encode(cb, bb, false);
        try {
        encoder.encode(cb);
        } catch (IllegalStateException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "reset",
            args = {}
        )
    })
    public void testInternalState() {
        CharBuffer in = CharBuffer.wrap("A");
        ByteBuffer out = ByteBuffer.allocate(0x10);
        encoder.reset();
        encoder.encode(in, out, false);
        in = CharBuffer.wrap("B");
        encoder.encode(in, out, true);
        encoder.flush(out);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testInternalState_Reset() {
        CharsetEncoder newEncoder = cs.newEncoder();
        newEncoder.reset();
        newEncoder.reset();
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            newEncoder.reset();
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.reset();
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            newEncoder.reset();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CoderMalfunctionError checking missed.",
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testInternalState_Encoding() {
        CharsetEncoder newEncoder = cs.newEncoder();
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();            
            newEncoder.encode(in, out, false);
        }
        {
            newEncoder.reset();            
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, false);
        }
        {
            newEncoder.reset();            
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
        {
            newEncoder.reset();            
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CoderMalfunctionError checking missed.",
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testInternalState_Encoding_END() {
        CharsetEncoder newEncoder = cs.newEncoder();
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();
            newEncoder.encode(in, out, true);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, true);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, true);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, true);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CoderMalfunctionError checking missed.",
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testInternalState_Flushed() {
        CharsetEncoder newEncoder = cs.newEncoder();
        {
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.flush(out);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.reset();
            newEncoder.flush(out);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Functional test.",
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Functional test.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Functional test.",
            method = "reset",
            args = {}
        )
    })
    public void testInternalState_Encode() throws CharacterCodingException {
        CharsetEncoder newEncoder = cs.newEncoder();
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.flush(out);
            out = newEncoder.encode(in);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "reset",
            args = {}
        )
    })
    public void testInternalState_from_Encode() throws CharacterCodingException {
        CharsetEncoder newEncoder = cs.newEncoder();
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            newEncoder.reset();
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            ByteBuffer out = ByteBuffer.allocate(0x10);
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            ByteBuffer out = ByteBuffer.allocate(0x10);
            try {
                newEncoder.encode(in, out, true);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = newEncoder.encode(in);
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
            }
        }
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
    }
}
