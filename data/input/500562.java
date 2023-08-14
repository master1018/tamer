@TestTargetClass(FilterWriter.class) 
public class FilterWriterTest extends junit.framework.TestCase {
    private boolean called;
    private FilterWriter fw;
    static class MyFilterWriter extends java.io.FilterWriter {
        public MyFilterWriter(java.io.Writer writer) {
            super(writer);
        }
    }
    class MockWriter extends java.io.Writer {
        public MockWriter() {
        }
        public void close() throws IOException {
            called = true;
        }
        public void flush() throws IOException {
            called = true;
        }
        public void write(char[] buffer, int offset, int count) throws IOException {
            called = true;
        }
        public void write(int oneChar) throws IOException {
            called = true;
        }
        public void write(String str, int offset, int count) throws IOException {
            called = true;
        }
        public long skip(long count) throws IOException {
            called = true;
            return 0;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies constructor FilterWriter(java.io.Writer).",
        method = "FilterWriter",
        args = {java.io.Writer.class}
    )     
    public void test_ConstructorLjava_io_Writer() {
        FilterWriter myWriter = null;
        called = true;
        try {
            myWriter = new MyFilterWriter(null);
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies close().",
        method = "close",
        args = {}
    )     
    public void test_close() throws IOException {
        fw.close();
        assertTrue("close() has not been called.", called);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies flush().",
        method = "flush",
        args = {}
    )     
    public void test_flush() throws IOException {
        fw.flush();
        assertTrue("flush() has not been called.", called);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies write(int).",
        method = "write",
        args = {int.class}
    )     
    public void test_writeI() throws IOException {
        fw.write(0);
        assertTrue("write(int) has not been called.", called);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies write(char[], int, int).",
        method = "write",
        args = {char[].class, int.class, int.class}
    )     
    public void test_write$CII() throws IOException {
        char[] buffer = new char[5];       
        fw.write(buffer, 0, 5);
        assertTrue("write(char[], int, int) has not been called.", called);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies write(char[], int, int).",
        method = "write",
        args = {char[].class, int.class, int.class}
    )     
    public void test_write$CII_Exception() throws IOException {
        char[] buffer = new char[10];
        fw = new MyFilterWriter(new OutputStreamWriter(
            new ByteArrayOutputStream()));
        try {
            fw.write(buffer, 0, -1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fw.write(buffer, -1, 1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fw.write(buffer, 10, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies write(String, int, int).",
        method = "write",
        args = {java.lang.String.class, int.class, int.class}
    )     
    public void test_writeLjava_lang_StringII() throws IOException {
        fw.write("Hello world", 0, 5);
        assertTrue("write(String, int, int) has not been called.", called);
    }
    protected void setUp() {
        fw = new MyFilterWriter(new MockWriter());
        called = false;
    }
    protected void tearDown() {
        try {
            fw.close();
        } catch (Exception e) {
            System.out.println("Exception during FilterWriterTest tear down.");
        }
    }
}
