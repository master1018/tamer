@TestTargetClass(CharsetEncoder.class)
public class AbstractCharsetEncoderTestCase extends TestCase {
    Charset cs;
    CharsetEncoder encoder;
    static final String unistr = " buffer";
    byte[] unibytes = new byte[] { 32, 98, 117, 102, 102, 101, 114 };
    byte[] defaultReplacement = new byte[] { 63 };
    byte[] specifiedReplacement = new byte[] { 26 };
    byte[] unibytesWithRep = null;
    byte[] surrogate = new byte[0];
    protected void setUp() throws Exception {
        super.setUp();
        encoder = cs.newEncoder();
        if (null == unibytesWithRep) {
            byte[] replacement = encoder.replacement();
            unibytesWithRep = new byte[replacement.length + unibytes.length];
            System.arraycopy(replacement, 0, unibytesWithRep, 0,
                    replacement.length);
            System.arraycopy(unibytes, 0, unibytesWithRep, replacement.length,
                    unibytes.length);
        }
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "canEncode",
        args = {char.class}
    )
    public void testCanEncodechar() throws CharacterCodingException {
        assertTrue(encoder.canEncode('\uc2c0'));
        assertTrue(encoder.canEncode('\ud800'));
        assertTrue(encoder.canEncode('\udc00'));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "reset",
            args = {}
        )
    })
    public void testResetIllegalState() throws CharacterCodingException {
        assertSame(encoder, encoder.reset());
        encoder.canEncode('\ud901');
        assertSame(encoder, encoder.reset());
        encoder.canEncode("\ud901\udc00");
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("aaa"));
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("aaa"), ByteBuffer.allocate(3), false);
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("aaa"), ByteBuffer.allocate(3), true);
        assertSame(encoder, encoder.reset());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testFlushIllegalState() throws CharacterCodingException {
        CharBuffer in = CharBuffer.wrap("aaa");
        ByteBuffer out = ByteBuffer.allocate(5);
        assertSame(encoder, encoder.reset());
        encoder.encode(in, out, true);
        out.rewind();
        CoderResult result = encoder.flush(out);
        try {
            encoder.flush(out);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertSame(encoder, encoder.reset());
        encoder.encode(in, out, false);
        try {
            encoder.flush(out);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testEncodeFacadeIllegalState() throws CharacterCodingException {
        CharBuffer in = CharBuffer.wrap("aaa");
        encoder.encode(in);
        in.rewind();
        encoder.encode(in);
        in.rewind();
        assertSame(encoder, encoder.reset());
        encoder.canEncode("\ud902\udc00");
        encoder.encode(in);
        in.rewind();
        assertSame(encoder, encoder.reset());
        encoder.canEncode('\ud902');
        encoder.encode(in);
        in.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
                ByteBuffer.allocate(30), true);
        encoder.encode(in);
        in.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
                ByteBuffer.allocate(30), false);
        encoder.encode(in);
        in.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
                ByteBuffer.allocate(30), true);
        encoder.flush(ByteBuffer.allocate(10));
        encoder.encode(in);
        in.rewind();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testEncodeTrueIllegalState() throws CharacterCodingException {
        CharBuffer in = CharBuffer.wrap("aaa");
        ByteBuffer out = ByteBuffer.allocate(5);
        encoder.encode(in, out, true);
        in.rewind();
        out.rewind();
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
                ByteBuffer.allocate(30), true);
        encoder.encode(in, out, true);
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
                ByteBuffer.allocate(30), false);
        encoder.encode(in, out, true);
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
                ByteBuffer.allocate(30), true);
        encoder.flush(ByteBuffer.allocate(10));
        try {
            encoder.encode(in, out, true);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        assertSame(encoder, encoder.reset());
        encoder.canEncode("\ud906\udc00");
        encoder.encode(in, out, true);
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.canEncode('\ud905');
        encoder.encode(in, out, true);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testEncodeFalseIllegalState() throws CharacterCodingException {
        CharBuffer in = CharBuffer.wrap("aaa");
        ByteBuffer out = ByteBuffer.allocate(5);
        encoder.encode(in, out, false);
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState1"));
        try {
            encoder.encode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
                ByteBuffer.allocate(30), true);
        try {
            encoder.encode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
                ByteBuffer.allocate(30), false);
        encoder.encode(in, out, false);
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
                ByteBuffer.allocate(30), true);
        encoder.flush(ByteBuffer.allocate(10));
        try {
            encoder.encode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        assertSame(encoder, encoder.reset());
        encoder.canEncode("\ud906\udc00");
        encoder.encode(in, out, false);
        in.rewind();
        out.rewind();
        assertSame(encoder, encoder.reset());
        encoder.canEncode('\ud905');
        encoder.encode(in, out, false);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {char.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    public void testCanEncodeIllegalState() throws CharacterCodingException {
        encoder.canEncode("\ud900\udc00");
        encoder.canEncode('\ud900');
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
                ByteBuffer.allocate(30), true);
        try {
            encoder.canEncode("\ud903\udc00");
            fail("should throw illegal state exception");
        } catch (IllegalStateException e) {
        }
        assertSame(encoder, encoder.reset());
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
                ByteBuffer.allocate(30), false);
        try {
            encoder.canEncode("\ud904\udc00");
            fail("should throw illegal state exception");
        } catch (IllegalStateException e) {
        }
        encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
                ByteBuffer.allocate(30), true);
        encoder.flush(ByteBuffer.allocate(10));
        encoder.canEncode("\ud905\udc00");
        encoder.canEncode('\ud906');
        assertSame(encoder, encoder.reset());
        encoder.canEncode("\ud906\udc00");
        encoder.canEncode('\ud905');
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    public void testCanEncodeCharSequence() {
        assertTrue(encoder.canEncode("\uc2c0"));
        assertTrue(encoder.canEncode("\ud800"));
        assertTrue(encoder.canEncode("\ud800\udc00"));
        assertTrue(encoder.canEncode("\ud800\udb00"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "encode",
        args = {java.nio.CharBuffer.class}
    )
    public void testEncodeCharBuffer() throws CharacterCodingException {
        try {
            encoder.encode(null);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        ByteBuffer out = encoder.encode(CharBuffer.wrap(""));
        assertEquals(out.position(), 0);
        assertByteArray(out, new byte[0]);
        out = encoder.encode(CharBuffer.wrap(unistr));
        assertEquals(out.position(), 0);
        assertByteArray(out, addSurrogate(unibytes));
        Charset cs = Charset.forName("UTF-8");
        CharsetEncoder encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder = encoder.replaceWith(new byte[] { (byte) 0xef, (byte) 0xbf,
                (byte) 0xbd, });
        CharBuffer in = CharBuffer.wrap("\ud800");
        out = encoder.encode(in);
        assertNotNull(out); 
    }
    private byte[] addSurrogate(byte[] expected) {
        if (surrogate.length > 0) {
            byte[] temp = new byte[surrogate.length + expected.length];
            System.arraycopy(surrogate, 0, temp, 0, surrogate.length);
            System.arraycopy(expected, 0, temp, surrogate.length,
                    expected.length);
            expected = temp;
        }
        return expected;
    }
    protected byte[] getEmptyByteArray() {
        return new byte[0];
    }
    CharBuffer getMalformedCharBuffer() {
        return CharBuffer.wrap("malform buffer");
    }
    CharBuffer getUnmapCharBuffer() {
        return CharBuffer.wrap("unmap buffer");
    }
    CharBuffer getExceptionCharBuffer() {
        return CharBuffer.wrap("runtime buffer");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "encode",
        args = {java.nio.CharBuffer.class}
    )
    public void testEncodeCharBufferException() throws CharacterCodingException {
        ByteBuffer out;
        CharBuffer in;
        in = getMalformedCharBuffer();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        if (in != null) {
            try {
                encoder.encode(in);
                fail("should throw MalformedInputException");
            } catch (MalformedInputException e) {
            }
            encoder.reset();
            in.rewind();
            encoder.onMalformedInput(CodingErrorAction.IGNORE);
            out = encoder.encode(in);
            assertByteArray(out, addSurrogate(unibytes));
            encoder.reset();
            in.rewind();
            encoder.onMalformedInput(CodingErrorAction.REPLACE);
            out = encoder.encode(in);
            assertByteArray(out, addSurrogate(unibytesWithRep));
        }
        in = getUnmapCharBuffer();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        if (in != null) {
            encoder.reset();
            try {
                encoder.encode(in);
                fail("should throw UnmappableCharacterException");
            } catch (UnmappableCharacterException e) {
            }
            encoder.reset();
            in.rewind();
            encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            out = encoder.encode(in);
            assertByteArray(out, unibytes);
            encoder.reset();
            in.rewind();
            encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            out = encoder.encode(in);
            assertByteArray(out, unibytesWithRep);
        }
        try {
            encoder.encode(getExceptionCharBuffer());
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
        }
    }
    void assertByteArray(ByteBuffer out, byte[] expected) {
        out = out.duplicate();
        if (out.position() != 0) {
            out.flip();
        }
        byte[] ba = new byte[out.limit() - out.position()];
        out.get(ba);
        assertTrue(Arrays.equals(ba, expected));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testEncodeCharBufferByteBufferboolean()
            throws CharacterCodingException {
        ByteBuffer out = ByteBuffer.allocate(200);
        CharBuffer in = CharBuffer.wrap(unistr);
        try {
            encoder.encode(null, out, true);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        try {
            encoder.encode(in, null, true);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        assertSame(encoder, encoder.reset());
        in.rewind();
        out.rewind();
        assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
        assertEquals(out.limit(), 200);
        assertTrue(out.position() > 0);
        assertTrue(out.remaining() > 0);
        assertEquals(out.capacity(), 200);
        assertByteArray(out, addSurrogate(unibytes));
        in.rewind();
        encoder.flush(out);
        assertSame(encoder, encoder.reset());
        in.rewind();
        out = ByteBuffer.allocate(200);
        assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, false));
        assertEquals(out.limit(), 200);
        assertTrue(out.position() > 0);
        assertTrue(out.remaining() > 0);
        assertEquals(out.capacity(), 200);
        assertByteArray(out, addSurrogate(unibytes));
        in.rewind();
        assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, false));
        in.rewind();
        assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
        assertEquals(out.limit(), 200);
        assertTrue(out.position() > 0);
        assertTrue(out.remaining() > 0);
        assertEquals(out.capacity(), 200);
        assertByteArray(out, addSurrogate(duplicateByteArray(unibytes, 3)));
        out = ByteBuffer.allocate(4);
        assertSame(encoder, encoder.reset());
        in.rewind();
        out.rewind();
        assertSame(CoderResult.OVERFLOW, encoder.encode(in, out, true));
        assertEquals(out.limit(), 4);
        assertEquals(out.position(), 4);
        assertEquals(out.remaining(), 0);
        assertEquals(out.capacity(), 4);
        ByteBuffer temp = ByteBuffer.allocate(200);
        out.flip();
        temp.put(out);
        out = temp;
        assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
        assertEquals(out.limit(), 200);
        assertTrue(out.position() > 0);
        assertTrue(out.remaining() > 0);
        assertEquals(out.capacity(), 200);
        assertByteArray(out, addSurrogate(unibytes));
        assertSame(encoder, encoder.reset());
        in.rewind();
        out = ByteBuffer.allocate(4);
        assertSame(CoderResult.OVERFLOW, encoder.encode(in, out, false));
        assertEquals(out.limit(), 4);
        assertEquals(out.position(), 4);
        assertEquals(out.remaining(), 0);
        assertEquals(out.capacity(), 4);
        temp = ByteBuffer.allocate(200);
        out.flip();
        temp.put(out);
        out = temp;
        assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, false));
        assertEquals(out.limit(), 200);
        assertTrue(out.position() > 0);
        assertTrue(out.remaining() > 0);
        assertEquals(out.capacity(), 200);
        assertByteArray(out, addSurrogate(unibytes));
    }
    void printByteBuffer(ByteBuffer buffer) {
        System.out.println("print buffer");
        if (buffer.position() != 0) {
            buffer.flip();
        }
        byte[] ba = buffer.array();
        for (int i = 0; i < ba.length; i++) {
            System.out.println(Integer.toHexString(ba[i]));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testEncodeCharBufferByteBufferbooleanExceptionFalse()
            throws CharacterCodingException {
        implTestEncodeCharBufferByteBufferbooleanException(false);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testEncodeCharBufferByteBufferbooleanExceptionTrue()
            throws CharacterCodingException {
        implTestEncodeCharBufferByteBufferbooleanException(true);
    }
    private byte[] duplicateByteArray(byte[] ba, int times) {
        byte[] result = new byte[ba.length * times];
        for (int i = 0; i < times; i++) {
            System.arraycopy(ba, 0, result, i * ba.length, ba.length);
        }
        return result;
    }
    protected void implTestEncodeCharBufferByteBufferbooleanException(
            boolean endOfInput) throws CharacterCodingException {
        ByteBuffer out = ByteBuffer.allocate(100);
        CharBuffer in = getMalformedCharBuffer();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        if (in != null) {
            encoder.reset();
            CoderResult r = encoder.encode(in, out, endOfInput);
            assertTrue(r.isMalformed());
            encoder.reset();
            out.clear();
            in.rewind();
            encoder.onMalformedInput(CodingErrorAction.IGNORE);
            assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
                    endOfInput));
            assertCodingErrorAction(endOfInput, out, in, unibytes);
            encoder.reset();
            out.clear();
            in.rewind();
            encoder.onMalformedInput(CodingErrorAction.REPLACE);
            assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
                    endOfInput));
            assertCodingErrorAction(endOfInput, out, in, unibytesWithRep);
        } else {
        }
        in = getUnmapCharBuffer();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        if (in != null) {
            encoder.reset();
            out.clear();
            assertTrue(encoder.encode(in, out, endOfInput).isUnmappable());
            encoder.reset();
            out.clear();
            in.rewind();
            encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
                    endOfInput));
            assertCodingErrorAction(endOfInput, out, in, unibytes);
            encoder.reset();
            out.clear();
            in.rewind();
            encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
                    endOfInput));
            assertCodingErrorAction(endOfInput, out, in, unibytesWithRep);
        } else {
        }
        try {
            encoder.encode(getExceptionCharBuffer());
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
        }
    }
    private void assertCodingErrorAction(boolean endOfInput, ByteBuffer out,
            CharBuffer in, byte[] expect) {
        if (endOfInput) {
            assertByteArray(out, addSurrogate(expect));
        } else {
            in.rewind();
            assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
                    endOfInput));
            in.rewind();
            assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
            assertByteArray(out, addSurrogate(duplicateByteArray(expect, 3)));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    public void testFlush() throws CharacterCodingException {
        ByteBuffer out = ByteBuffer.allocate(6);
        CharBuffer in = CharBuffer.wrap("aaa");
        assertEquals(in.remaining(), 3);
        encoder.encode(CharBuffer.wrap("testFlush"), ByteBuffer.allocate(20),
                true);
        assertSame(CoderResult.UNDERFLOW, encoder
                .flush(ByteBuffer.allocate(50)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "isLegalReplacement",
        args = {byte[].class}
    )
    public void testIsLegalReplacement() {
        try {
            encoder.isLegalReplacement(null);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }
        assertTrue(encoder.isLegalReplacement(specifiedReplacement));
        assertTrue(encoder.isLegalReplacement(new byte[200]));
        byte[] ba = getIllegalByteArray();
        if (ba != null) {
            assertFalse(encoder.isLegalReplacement(ba));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "isLegalReplacement",
        args = {byte[].class}
    )
    public void testIsLegalReplacementEmptyArray() {
        assertTrue(encoder.isLegalReplacement(new byte[0]));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMalformedInput",
            args = {java.nio.charset.CodingErrorAction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "malformedInputAction",
            args = {}
        )
    })
    public void testOnMalformedInput() {
        assertSame(CodingErrorAction.REPORT, encoder.malformedInputAction());
        try {
            encoder.onMalformedInput(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        encoder.onMalformedInput(CodingErrorAction.IGNORE);
        assertSame(CodingErrorAction.IGNORE, encoder.malformedInputAction());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUnmappableCharacter",
            args = {java.nio.charset.CodingErrorAction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unmappableCharacterAction",
            args = {}
        )
    })
    public void testOnUnmappableCharacter() {
        assertSame(CodingErrorAction.REPORT, encoder
                .unmappableCharacterAction());
        try {
            encoder.onUnmappableCharacter(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
        assertSame(CodingErrorAction.IGNORE, encoder
                .unmappableCharacterAction());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "replaceWith",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "replacement",
            args = {}
        )
    })
    public void testReplacement() {
        try {
            encoder.replaceWith(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            encoder.replaceWith(new byte[0]);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            encoder.replaceWith(new byte[100]);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        byte[] nr = getLegalByteArray();
        assertSame(encoder, encoder.replaceWith(nr));
        assertSame(nr, encoder.replacement());
        nr = getIllegalByteArray();
        try {
            encoder.replaceWith(new byte[100]);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
    }
    protected byte[] getLegalByteArray() {
        return new byte[] { 'a' };
    }
    protected byte[] getIllegalByteArray() {
        return new byte[155];
    }
}
