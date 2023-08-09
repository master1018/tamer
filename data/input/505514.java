@TestTargetClass(SequenceInputStream.class) 
public class SequenceInputStreamTest extends junit.framework.TestCase {
    Support_ASimpleInputStream simple1, simple2;
    SequenceInputStream si;
    String s1 = "Hello";
    String s2 = "World";
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Checks NullPointerException. A positive test of this " +
                "constructor is implicitely done in setUp(); if it would " +
                "fail, all other tests will also fail.",
        method = "SequenceInputStream",
        args = {java.io.InputStream.class, java.io.InputStream.class}
    )
    public void test_Constructor_LInputStreamLInputStream_Null() {        
        try {
            si = new SequenceInputStream(null , null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
        InputStream is = new ByteArrayInputStream(s1.getBytes()); 
        si = new SequenceInputStream(is , null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SequenceInputStream",
        args = {java.util.Enumeration.class}
    )
    public void test_ConstructorLjava_util_Enumeration() {
        class StreamEnumerator implements Enumeration<InputStream> {
            InputStream streams[] = new InputStream[2];
            int count = 0;
            public StreamEnumerator() {
                streams[0] = new ByteArrayInputStream(s1.getBytes());
                streams[1] = new ByteArrayInputStream(s2.getBytes());
            }
            public boolean hasMoreElements() {
                return count < streams.length;
            }
            public InputStream nextElement() {
                return streams[count++];
            }
        }
        try {
            si = new SequenceInputStream(new StreamEnumerator());
            byte buf[] = new byte[s1.length() + s2.length()];
            si.read(buf, 0, s1.length());
            si.read(buf, s1.length(), s2.length());
            assertTrue("Read incorrect bytes: " + new String(buf), new String(
                    buf).equals(s1 + s2));
        } catch (IOException e) {
            fail("IOException during read test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "available",
        args = {}
    )
    public void test_available() throws IOException {
        assertEquals("Returned incorrect number of bytes!", s1.length(), si.available());
        simple2.throwExceptionOnNextUse = true;
        assertTrue("IOException on second stream should not affect at this time!",
                si.available() == s1.length());
        simple1.throwExceptionOnNextUse = true;
        try {
            si.available();
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    public void test_close() throws IOException {
        assertTrue("Something is available!", si.available() > 0);
        si.close();
        si.close();
        assertTrue("Nothing is available, now!", si.available() <= 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    public void test_close2() throws IOException {
        simple1.throwExceptionOnNextUse = true;
        try {
            si.close();
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public void test_read() throws IOException {
        si.read();
        assertEquals("Test 1: Incorrect char read;", 
                s1.charAt(1), (char) si.read());
        simple2.throwExceptionOnNextUse = true;
        try {
            assertEquals("Test 2: Incorrect char read;", 
                    s1.charAt(2), (char) si.read());
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException.");
        }
        simple1.throwExceptionOnNextUse = true;
        try {
            si.read();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
        }
        simple1.throwExceptionOnNextUse = false;
        si.read();
        si.read();
        try {
            si.read();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
        }
        simple2.throwExceptionOnNextUse = false;
        try {
            assertEquals("Test 6: Incorrect char read;", 
                    s2.charAt(0), (char) si.read());
        } catch (IOException e) {
            fail("Test 7: Unexpected IOException.");
        }
        si.close();
        assertTrue("Test 8: -1 expected when reading from a closed " + 
                   "sequence input stream.", si.read() == -1);        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read_exc() throws IOException {
        simple2.throwExceptionOnNextUse = true;
        assertEquals("IOException on second stream should not affect at this time!", 72, si.read());
        simple1.throwExceptionOnNextUse = true;
        try {
            si.read();
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII() throws IOException {
        try {
            byte buf[] = new byte[s1.length() + s2.length()];
            si.read(buf, 0, s1.length());
            si.read(buf, s1.length(), s2.length());
            assertTrue("Read incorrect bytes: " + new String(buf), new String(
                    buf).equals(s1 + s2));
        } catch (IOException e) {
            fail("IOException during read test : " + e.getMessage());
        }
        ByteArrayInputStream bis1 = new ByteArrayInputStream(
                new byte[] { 1, 2, 3, 4 });
        ByteArrayInputStream bis2 = new ByteArrayInputStream(
                new byte[] { 5, 6, 7, 8 });
        SequenceInputStream sis = new SequenceInputStream(bis1, bis2);
        try {
            sis.read(null, 0, 2);
            fail("Expected NullPointerException exception");
        } catch (NullPointerException e) {
        }
        assertEquals(4, sis.read(new byte[10], 0, 8));
        assertEquals(5, sis.read());
        byte[] array = new byte[] { 1 , 2 , 3 ,4 };
        sis.close();
        int result = sis.read(array , 0 , 5);
        assertEquals(-1 , result);    
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII_Excpetion() throws IOException {
        byte[] buf = new byte[4];
        si.read(buf, 0, 2);
        si.read(buf, 2, 1);
        simple2.throwExceptionOnNextUse = true;
        si.read(buf, 3, 1);
        assertEquals("Wrong stuff read!", "Hell", new String(buf));
        simple1.throwExceptionOnNextUse = true;
        try {
            si.read(buf, 3, 1);
            fail("IOException not thrown!");
        } catch (IOException e) {
        }
        buf = new byte[10];
        simple1 = new Support_ASimpleInputStream(s1);
        simple2 = new Support_ASimpleInputStream(s2);
        si = new SequenceInputStream(simple1, simple2);
        try {
            si.read(buf, -1, 1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            si.read(buf, 0, -1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            si.read(buf, 1, 10);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    protected void setUp() {
        simple1 = new Support_ASimpleInputStream(s1);
        simple2 = new Support_ASimpleInputStream(s2);
        si = new SequenceInputStream(simple1, simple2);
    }
    protected void tearDown() {
    }
}
