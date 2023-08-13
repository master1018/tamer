@TestTargetClass(Reader.class)
public class ReaderTest extends TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Reader",
            args = {}
        )
    public void test_Reader() {
        MockReader r = new MockReader();
        assertTrue("Test 1: Lock has not been set correctly.", r.lockSet(r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "Reader",
        args = {java.lang.Object.class}
    )
    public void test_Reader_CharBuffer_null() throws IOException {
        String s = "MY TEST STRING";
        MockReader mockReader = new MockReader(s.toCharArray());
        CharBuffer charBuffer = null;
        try {
            mockReader.read(charBuffer);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets ({
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Functional test.",
                method = "Reader",
                args = {java.lang.Object.class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Functional test.",
                method = "read",
                args = {java.nio.CharBuffer.class}
        )
    })
    public void test_Reader_CharBuffer_ZeroChar() throws IOException {
        String s = "MY TEST STRING";
        char[] srcBuffer = s.toCharArray();
        MockReader mockReader = new MockReader(srcBuffer);
        CharBuffer charBuffer = CharBuffer.allocate(0);
        int result = mockReader.read(charBuffer);
        assertEquals(0, result);
        char[] destBuffer = new char[srcBuffer.length];
        mockReader.read(destBuffer);
        assertEquals(s, String.valueOf(destBuffer));
    }
    @TestTargets ({
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Functional test.",
                method = "Reader",
                args = {java.lang.Object.class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Functional test.",
                method = "read",
                args = {java.nio.CharBuffer.class}
        )
    })
    public void test_Reader_CharBufferChar() throws IOException {
        String s = "MY TEST STRING";
        char[] srcBuffer = s.toCharArray();
        final int CHARBUFFER_SIZE = 10;
        MockReader mockReader = new MockReader(srcBuffer);
        CharBuffer charBuffer = CharBuffer.allocate(CHARBUFFER_SIZE);
        charBuffer.append('A');
        final int CHARBUFFER_REMAINING = charBuffer.remaining();
        int result = mockReader.read(charBuffer);
        assertEquals(CHARBUFFER_REMAINING, result);
        charBuffer.rewind();
        assertEquals(s.substring(0, CHARBUFFER_REMAINING), charBuffer
                .subSequence(CHARBUFFER_SIZE - CHARBUFFER_REMAINING,
                        CHARBUFFER_SIZE).toString());
        char[] destBuffer = new char[srcBuffer.length - CHARBUFFER_REMAINING];
        mockReader.read(destBuffer);
        assertEquals(s.substring(CHARBUFFER_REMAINING), String
                .valueOf(destBuffer));
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        CharBuffer buf = CharBuffer.allocate(4);
        assertEquals("Wrong return value!", 4, simple.read(buf));
        buf.rewind();
        assertEquals("Wrong stuff read!", "Bla ", String.valueOf(buf));
        simple.read(buf);
        buf.rewind();
        assertEquals("Wrong stuff read!", "bla,", String.valueOf(buf));
        simple.throwExceptionOnNextUse = true;
        try {
            simple.read(buf);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {char[].class}
    )
    public void test_Read_$C() throws IOException {
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        char[] buf = new char[4];
        assertEquals("Wrong return value!", 4, simple.read(buf));
        assertEquals("Wrong stuff read!", "Bla ", new String(buf));
        simple.read(buf);
        assertEquals("Wrong stuff read!", "bla,", new String(buf));
        simple.throwExceptionOnNextUse = true;
        try {
            simple.read(buf);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "mark",
        args = {int.class}
    )
    public void test_mark() {
        MockReader mockReader = new MockReader();
        try {
            mockReader.mark(0);
            fail("Should throw IOException for Reader do not support mark");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "markSupported",
        args = {}
    )
    public void test_markSupported() {
        assertFalse("markSupported must return false", new MockReader().markSupported());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public void test_read() throws IOException {
        MockReader reader = new MockReader();
        assertEquals("Should be equal to -1", -1, reader.read());
        String string = "MY TEST STRING";
        char[] srcBuffer = string.toCharArray();
        MockReader mockReader = new MockReader(srcBuffer);
        for (char c : srcBuffer) {
            assertEquals("Should be equal to \'" + c + "\'", c, mockReader
                    .read());
        }
        mockReader.read();
        assertEquals("Should be equal to -1", -1, reader.read());
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        int res;
        res = simple.read();
        assertEquals("Wrong stuff read!", 'B', res);
        res = simple.read();
        assertEquals("Wrong stuff read!", 'l', res);
        simple.throwExceptionOnNextUse = true;
        try {
            simple.read();
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ready",
        args = {}
    )
    public void test_ready() throws IOException {
        MockReader mockReader = new MockReader();
        assertFalse("Should always return false", mockReader.ready());
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        simple.throwExceptionOnNextUse = true;
        try {
            simple.ready();
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() throws IOException {
        MockReader mockReader = new MockReader();
        try {
            mockReader.reset();
            fail("Should throw IOException");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "skip",
        args = {long.class}
    )
    public void test_skip() throws IOException {
        String string = "MY TEST STRING";
        char[] srcBuffer = string.toCharArray();
        int length = srcBuffer.length;
        MockReader mockReader = new MockReader(srcBuffer);
        assertEquals("Should be equal to \'M\'", 'M', mockReader.read());
        mockReader.skip(length / 2);
        assertEquals("Should be equal to \'S\'", 'S', mockReader.read());
        mockReader.skip(length);
        try {
            mockReader.skip(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        char[] buf = new char[4];
        simple.read(buf);
        assertEquals("Wrong stuff read!", "Bla ", new String(buf));
        simple.skip(5);
        simple.read(buf);
        assertEquals("Wrong stuff read!", "what", new String(buf));
        simple.throwExceptionOnNextUse = true;
        try {
            simple.skip(1);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    class MockReader extends Reader {
        private char[] contents;
        private int current_offset = 0;
        private int length = 0;
        public MockReader() {
            super();
        }
        public MockReader(char[] data) {
            contents = data;
            length = contents.length;
        }
        @Override
        public void close() throws IOException {
            contents = null;
        }
        @Override
        public int read(char[] buf, int offset, int count) throws IOException {
            if (null == contents) {
                return -1;
            }
            if (length <= current_offset) {
                return -1;
            }
            if (buf.length < offset + count) {
                throw new IndexOutOfBoundsException();
            }
            count = Math.min(count, length - current_offset);
            for (int i = 0; i < count; i++) {
                buf[offset + i] = contents[current_offset + i];
            }
            current_offset += count;
            return count;
        }
        public boolean lockSet(Object o) {
            return (lock == o);
        }
    }
}
