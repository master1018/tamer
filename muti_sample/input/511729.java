@TestTargetClass(Writer.class) 
public class WriterTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {char.class}
    )
    public void test_appendChar() throws IOException {
        char testChar = ' ';
        MockWriter writer = new MockWriter(20);
        writer.append(testChar);
        assertEquals(String.valueOf(testChar), String.valueOf(writer
                .getContents()));
        writer.close();
        Writer tobj = new Support_ASimpleWriter(2);
        tobj.append('a');
        tobj.append('b');
        assertEquals("Wrong stuff written!", "ab", tobj.toString());
        try {
            tobj.append('c');
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "append",
        args = {java.lang.CharSequence.class}
    )
    public void test_appendCharSequence() throws IOException {
        String testString = "My Test String";
        MockWriter writer = new MockWriter(20);
        writer.append(testString);
        assertEquals(testString, String.valueOf(writer.getContents()));
        writer.close();
        Writer tobj = new Support_ASimpleWriter(20);
        tobj.append(testString);
        assertEquals("Wrong stuff written!", testString, tobj.toString());
        try {
            tobj.append(testString);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "append",
        args = {CharSequence.class, int.class, int.class}
    )
    public void test_appendCharSequenceIntInt() throws IOException {
        String testString = "My Test String";
        MockWriter writer = new MockWriter(20);
        writer.append(testString, 1, 3);
        assertEquals(testString.substring(1, 3), String.valueOf(writer
                .getContents()));
        writer.close();
        Writer tobj = new Support_ASimpleWriter(21);
        testString = "0123456789abcdefghijABCDEFGHIJ";
        tobj.append(testString, 0, 5);
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.append(testString, 10, 15);
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.append(testString, 20, 30);
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        try {
            tobj.append(testString, 30, 31);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
        tobj.append(testString, 20, 21); 
        try {
            tobj.append(testString, 29, 30);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "append",
        args = {CharSequence.class, int.class, int.class}
    )
    public void test_appendCharSequenceIntInt_Exception() throws IOException {
        String testString = "My Test String";
        Writer tobj = new Support_ASimpleWriter(21);
        try {
            tobj.append(testString, 30, 31);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            tobj.append(testString, -1, 1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            tobj.append(testString, 0, -1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {char[].class}
    )
    public void test_write$C() throws IOException {
        Writer tobj = new Support_ASimpleWriter(21);
        tobj.write("01234".toCharArray());
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.write("abcde".toCharArray());
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.write("ABCDEFGHIJ".toCharArray());
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        tobj.write("z".toCharArray()); 
        try {
            tobj.write("LES JEUX SONT FAITS".toCharArray());
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public void test_writeI() throws IOException {
        Writer tobj = new Support_ASimpleWriter(2);
        tobj.write('a');
        tobj.write('b');
        assertEquals("Wrong stuff written!", "ab", tobj.toString());
        try {
            tobj.write('c');
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {java.lang.String.class}
    )
    public void test_writeLjava_lang_String() throws IOException {
        Writer tobj = new Support_ASimpleWriter(21);
        tobj.write("01234");
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.write("abcde");
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.write("ABCDEFGHIJ");
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        tobj.write("z"); 
        try {
            tobj.write("LES JEUX SONT FAITS");
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {String.class, int.class, int.class}
    )
    public void test_writeLjava_lang_StringII() throws IOException {
        String testString;
        Writer tobj = new Support_ASimpleWriter(21);
        testString = "0123456789abcdefghijABCDEFGHIJ";
        tobj.write(testString, 0, 5);
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.write(testString, 10, 5);
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.write(testString, 20, 10);
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        try {
            tobj.write(testString, 30, 1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
        tobj.write(testString, 20, 1); 
        try {
            tobj.write(testString, 29, 1);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {String.class, int.class, int.class}
    )
    public void test_writeLjava_lang_StringII_Exception() throws IOException {
        String testString = "My Test String";
        Writer tobj = new Support_ASimpleWriter(21);
        try {
            tobj.write(testString, 30, 31);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            tobj.write(testString, -1, 1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            tobj.write(testString, 0, -1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    class MockWriter extends Writer {
        private char[] contents;
        private int length;
        private int offset;
        MockWriter(int capacity) {
            contents = new char[capacity];
            length = capacity;
            offset = 0;
        }
        public synchronized void close() throws IOException {
            flush();
            contents = null;
        }
        public synchronized void flush() throws IOException {
        }
        public void write(char[] buffer, int offset, int count)
                throws IOException {
            if (null == contents) {
                throw new IOException();
            }
            if (offset < 0 || count < 0 || offset >= buffer.length) {
                throw new IndexOutOfBoundsException();
            }
            count = Math.min(count, buffer.length - offset);
            count = Math.min(count, this.length - this.offset);
            for (int i = 0; i < count; i++) {
                contents[this.offset + i] = buffer[offset + i];
            }
            this.offset += count;
        }
        public char[] getContents() {
            char[] result = new char[offset];
            for (int i = 0; i < offset; i++) {
                result[i] = contents[i];
            }
            return result;
        }
    }
}
