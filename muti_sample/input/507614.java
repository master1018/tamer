@TestTargetClass(StringBufferInputStream.class) 
public class StringBufferInputStreamTest extends junit.framework.TestCase {
    StringBufferInputStream sbis;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "StringBufferInputStream",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            new StringBufferInputStream("");
        } catch (Exception ee) {
            fail("Exception " + ee.getMessage() + " does not expected in this case");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "available",
        args = {}
    )
    public void test_available() {
        assertEquals("Returned incorrect number of available bytes", 11, sbis
                .available());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII() {
        byte[] buf = new byte[5];
        sbis.skip(6);
        sbis.read(buf, 0, 5);
        assertEquals("Returned incorrect chars", "World", new String(buf));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII_Exception() {
        byte[] buf = new byte[10];
        try {
            sbis.read(buf, 0, -1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sbis.read(buf, -1, 1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            sbis.read(buf, 10, 1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public void test_read() {
        assertEquals("Read returned incorrect char", 'H', sbis.read());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() {
        long s = sbis.skip(6);
        assertEquals("Unable to skip correct umber of chars", 6, s);
        sbis.reset();
        assertEquals("Failed to reset", 'H', sbis.read());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "skip",
        args = {long.class}
    )
    public void test_skipJ() {
        long s = sbis.skip(6);
        assertEquals("Unable to skip correct umber of chars", 6, s);
        assertEquals("Skip positioned at incorrect char", 'W', sbis.read());
    }
    protected void setUp() {
        sbis = new StringBufferInputStream("Hello World");
    }
    protected void tearDown() {
    }
}
