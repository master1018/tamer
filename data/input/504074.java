@TestTargetClass(InputStream.class) 
public class InputStreamTest extends junit.framework.TestCase {
    public static final String testString = "Lorem ipsum dolor sit amet,\n" +
        "consectetur adipisicing elit,\nsed do eiusmod tempor incididunt ut" +
        "labore et dolore magna aliqua.\n";
    private InputStream is;
    class MockInputStream extends java.io.InputStream {
        private byte[] input;
        private int position;
        public MockInputStream() {
            super();
            input = testString.getBytes();
            position = 0;
        }
        public int read() throws IOException {
            int result = -1;
            if (position < input.length) {
                result = input[position];
                position++;
            }
            return result;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies constructor InputStream(). Since this constructor does nothing, this test is intentionally kept basic.",
        method = "InputStream",
        args = {}
    )     
    public void test_Constructor() {
        try {
            InputStream myIS = new MockInputStream();
            myIS.close();
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that available() returns 0.",
        method = "available",
        args = {}
    )     
    public void test_available() throws IOException {
        assertEquals(is.available(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies close(). Since this method does nothing, this test is intentionally kept basic.",
        method = "close",
        args = {}
    )     
    public void test_close() {
        try {
            is.close();
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies mark(int). Since this method does nothing, this test is intentionally kept basic.",
        method = "mark",
        args = {int.class}
    )     
    public void test_markI() {
        try {
            is.mark(10);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that markSupported() returns false.",
        method = "markSupported",
        args = {}
    )     
    public void test_markSupported() throws IOException {
        assertFalse("markSupported() has returned the wrong default value.", 
                is.markSupported());
    }
    @TestTargets (
            { @TestTargetNew(
                    level = TestLevel.COMPLETE,
                    notes = "Verifies read(byte[]).",
                    method = "read",
                    args = {byte[].class}
              ),     
              @TestTargetNew(
                    level = TestLevel.COMPLETE,
                    notes = "Verifies ObjectInput.read(byte[]) since " +
                            "ObjectInputStream inherits the implementation " +
                            "of read(byte[]) from InputStream.",
                    clazz = ObjectInput.class,
                    method = "read",
                    args = {byte[].class}
              )
            }
    )
    public void test_read$B() throws IOException {
        byte[] b = new byte[10];
        byte[] ref = testString.getBytes();
        boolean equal = true;
        int bytesRead = 0;
        int i;
        assertEquals("Test 1: Incorrect count of bytes read.", 
                is.read(b), 10);
        for (i = 0; i < 10; i++) {
            equal &= (b[i] == ref[i]);
        }
        assertTrue("Test 1: Wrong bytes read.", equal);
        b = new byte[ref.length];
        bytesRead = is.read(b); 
        assertEquals("Test 2: Incorrect count of bytes read.", 
                bytesRead, ref.length - 10);
        for (i = 0; i < bytesRead; i++) {
            equal &= (b[i] == ref[i + 10]);
        }
        assertTrue("Test 2: Wrong bytes read.", equal);
        bytesRead = is.read(b); 
        assertEquals("Test 3:", bytesRead, -1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies argument checking of read(byte[], int, int).",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )     
    public void test_read$BII_Exception() throws IOException {
        byte[] b = new byte[10];
        int bytesRead = 0;
        try {
            bytesRead = is.read(b, -1, 5);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            bytesRead = is.read(b, 5, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            bytesRead = is.read(b, 6, 5);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            bytesRead = is.read(b, 6, 4);
        } catch (IndexOutOfBoundsException e) {
            fail("Test 4: Unexpected IndexOutOfBoundsException.");
        }
        assertEquals("Test 4:", bytesRead, 4);
        try {
            bytesRead = is.read(b, 6, 0);
        } catch (Exception e) {
            fail("Test 5: Unexpected Exception " + e.getMessage());
        }
        assertEquals("Test 5:", bytesRead, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies read(byte[], int, int).",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )     
    public void test_read$BII() throws IOException {
        byte[] b = new byte[10];
        byte[] ref = testString.getBytes();
        boolean equal = true;
        int bytesRead = 0;
        int i;
        assertEquals("Test 1: Incorrect count of bytes read.", 
                is.read(b, 0, 5), 5);
        for (i = 0; i < 5; i++) {
            equal &= (b[i] == ref[i]);
        }
        assertTrue("Test 1: Wrong bytes read.", equal);
        assertEquals("Test 2: Incorrect count of bytes read.", 
                is.read(b, 5, 3), 3);
        for (i = 5; i < 8; i++) {
            equal &= (b[i] == ref[i]);
        }
        assertTrue("Test 2: Wrong bytes read.", equal);
        b = new byte[ref.length];
        bytesRead = is.read(b, 2, b.length - 2); 
        assertEquals("Test 3: Incorrect count of bytes read.", 
                bytesRead, ref.length - 8);
        assertEquals("Test 3:", b[0], 0);
        assertEquals("Test 3:", b[1], 0);
        for (i = 0; i < bytesRead; i++) {
            equal &= (b[i + 2] == ref[i + 8]);
        }
        assertTrue("Test 2: Wrong bytes read.", equal);
        assertEquals("Test 4:", is.read(b, 0, 2), -1); 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that reset() throws an IOException.",
        method = "reset",
        args = {}
    )     
    public void test_reset() {
        try {
            is.reset();
            fail("IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargets (
            { @TestTargetNew(
                    level = TestLevel.COMPLETE,
                    notes = "Verifies skip(long).",
                    method = "skip",
                    args = {long.class}
              ),
              @TestTargetNew(
                    level = TestLevel.COMPLETE,
                    notes = "Verifies ObjectInput.skip(long) since " +
                            "ObjectInputStream inherits the implementation " +
                            "of skip(long) from InputStream.",
                    clazz = ObjectInput.class,
                    method = "skip",
                    args = {long.class}
              )
            }
    )     
    public void test_skipL() throws IOException {
        byte[] b = new byte[12];
        byte[] ref = testString.getBytes();
        int i;
        boolean equal = true;
        assertEquals("Test 1:", is.skip(-42), 0);
        assertEquals("Test 1: Incorrect count of bytes read.", 
                is.read(b), 12);
        for (i = 0; i < 12; i++) {
            equal &= (b[i] == ref[i]);
        }
        assertTrue("Test 1: Wrong bytes read.", equal);
        assertEquals("Test 2: Incorrect count of bytes skipped.", 
                is.skip(17), 17);
        is.read(b, 0, 10);
        for (i = 0; i < 10; i++) {
            equal &= (b[i] == ref[i + 29]);
        }
        assertTrue("Test 3: Wrong bytes read.", equal);
        assertEquals("Test 4: Incorrect count of bytes skipped.", 
                is.skip(ref.length), ref.length - 39); 
        assertEquals("Test 5:", is.read(b), -1);
        assertEquals("Test 5:", is.skip(10), 0);
    }
    protected void setUp() {
        is = new MockInputStream();
    }
    protected void tearDown() {
        try {
            is.close();
        } catch (Exception e) {
        }
    }
}
