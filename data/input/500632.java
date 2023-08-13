@TestTargetClass(CharsetDecoder.class)
public class CharsetEncoderDecoderBufferTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "decode",
            args = {}
        )
    })
    public void testDecoderOutputBuffer() {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        char[] cBuf = new char[10];
        CharBuffer out = CharBuffer.wrap(cBuf);
        assertTrue(out.hasArray());
        decoder.decode(ByteBuffer.wrap(new byte[]{(byte)'a', (byte)'b', (byte)'c', (byte)'d'}),
                       out, false);
        assertEquals("abcd", new String(cBuf, 0, 4));
        assertEquals(0, cBuf[4]);
        assertEquals(0, cBuf[5]);
        byte[] bBuf = new byte[10];
        out = ByteBuffer.wrap(bBuf).asCharBuffer();
        assertFalse(out.hasArray());
        decoder.decode(ByteBuffer.wrap(new byte[]{(byte)'x'}), out, true);
        assertEquals('x', bBuf[1]);
        assertEquals(0, bBuf[3]);
        assertEquals("abcd", new String(cBuf, 0, 4));
        assertEquals(0, cBuf[4]);
        assertEquals(0, cBuf[5]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "decode",
            args = {}
        )
    })
    public void testDecoderInputBuffer() {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        CharBuffer out = CharBuffer.wrap(new char[10]);
        byte[] inArray = {(byte)'a', (byte)'b'};
        ByteBuffer inWithArray = ByteBuffer.wrap(inArray);
        assertTrue(inWithArray.hasArray());
        decoder.decode(inWithArray, out, false);
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);
        ByteBuffer inWithoutArray = ByteBuffer.allocateDirect(1);
        inWithoutArray.put(0, (byte)'x');
        assertFalse(inWithoutArray.hasArray());
        decoder.decode(inWithoutArray, out, true);
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "encode",
            args = {}
        )
    })
    public void testEncoderOutputBuffer() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        byte[] buffer = new byte[10];
        ByteBuffer out = ByteBuffer.wrap(buffer);
        assertTrue(out.hasArray());
        encoder.encode(CharBuffer.wrap("ab"), out, false);
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals(0, buffer[2]);
        out = ByteBuffer.allocateDirect(10);
        assertFalse(out.hasArray());
        encoder.encode(CharBuffer.wrap("x"), out, true);
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals(0, buffer[2]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "encode",
            args = {}
        )
    })
    public void testEncoderInputBuffer() {
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        ByteBuffer out = ByteBuffer.wrap(new byte[10]);
        char[] inArray = {'a', 'b'};
        CharBuffer inWithArray = CharBuffer.wrap(inArray);
        assertTrue(inWithArray.hasArray());
        encoder.encode(inWithArray, out, false);
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);
        CharBuffer inWithoutArray = CharBuffer.wrap("x");
        assertFalse(inWithoutArray.hasArray());
        encoder.encode(inWithoutArray, out, true);
        assertEquals('a', inArray[0]);
        assertEquals('b', inArray[1]);
    }
}
