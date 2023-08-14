@TestTargetClass(FilterInputStream.class) 
public class FilterInputStreamTest extends junit.framework.TestCase {
    static class MyFilterInputStream extends java.io.FilterInputStream {
        public MyFilterInputStream(java.io.InputStream is) {
            super(is);
        }
    }
    private String fileName;
    private FilterInputStream is;
    byte[] ibuf = new byte[4096];
    private static final String testString = "Lorem ipsum dolor sit amet,\n" +
    "consectetur adipisicing elit,\nsed do eiusmod tempor incididunt ut" +
    "labore et dolore magna aliqua.\n";
    private static final int testLength = testString.length();
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies constructor FilterInputStream(InputStream).",
        method = "FilterInputStream",
        args = {java.io.InputStream.class}
    )     
    public void test_Constructor() {
        try {
            is.close();
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        } catch (NullPointerException npe) {
            fail("Unexpected NullPointerException.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "available",
        args = {}
    )     
    public void test_available() throws IOException {
        assertEquals("Test 1: Returned incorrect number of available bytes;", 
                testLength, is.available());
        is.close();
        try {
            is.available();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "close",
        args = {}
    )     
    public void test_close() throws IOException {
        is.close();
        try {
            is.read();
            fail("Test 1: Read from closed stream succeeded.");
        } catch (IOException e) {
        }
        Support_ASimpleInputStream sis = new Support_ASimpleInputStream(true);
        is = new MyFilterInputStream(sis);
        try {
            is.close();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
        }
        sis.throwExceptionOnNextUse = false;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies mark(int) in combination with reset().",
            method = "mark",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies mark(int) in combination with reset().",
            method = "reset",
            args = {}
        )
    })     
    public void test_markI() throws Exception {
        final int bufSize = 10;
        byte[] buf1 = new byte[bufSize];
        byte[] buf2 = new byte[bufSize];
        is.read(buf1, 0, bufSize);
        is.mark(2 * bufSize);
        is.read(buf1, 0, bufSize);
        try {
            is.reset();
        } catch (IOException e) {
        }
        is.read(buf2, 0, bufSize);
        assertFalse("Test 1: mark() should have no effect.", 
                Arrays.equals(buf1, buf2));
        is.close();
        is = new MyFilterInputStream(new BufferedInputStream(
                new java.io.FileInputStream(fileName), 100));
        is.read(buf1, 0, bufSize);
        is.mark(2 * bufSize);
        is.read(buf1, 0, bufSize);
        is.reset();
        is.read(buf2, 0, bufSize);
        assertTrue("Test 2: mark() or reset() has failed.", 
                Arrays.equals(buf1, buf2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies markSupported() method.",
        method = "markSupported",
        args = {}
    )     
    public void test_markSupported() throws Exception {
        assertFalse("Test 1: markSupported() incorrectly returned true " +
                "for a FileInputStream.", is.markSupported());
        is.close();
        is = new MyFilterInputStream(new BufferedInputStream(
                new java.io.FileInputStream(fileName), 100));
        assertTrue("Test 2: markSupported() incorrectly returned false " +
                "for a BufferedInputStream.", is.markSupported());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "read",
        args = {}
    )    
    public void test_read() throws IOException {
        int c = is.read();
        assertEquals("Test 1: Read returned incorrect char;", 
                testString.charAt(0), c);
        is.close();
        try {
            is.read();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "read",
        args = {byte[].class}
    )    
    public void test_read$B() throws IOException {
        byte[] buf1 = new byte[100];
        is.read(buf1);
        assertTrue("Test 1: Failed to read correct data.", 
                new String(buf1, 0, buf1.length).equals(
                        testString.substring(0, 100)));
        is.close();
        try {
            is.read(buf1);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "read",
        args = {byte[].class, int.class, int.class}
    )        
    public void test_read$BII() throws IOException {
        byte[] buf1 = new byte[20];
        is.skip(10);
        is.read(buf1, 0, buf1.length);
        assertTrue("Failed to read correct data", new String(buf1, 0,
                buf1.length).equals(testString.substring(10, 30)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Illegal argument checks.",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )      
    public void test_read$BII_Exception() throws IOException {
        byte[] buf = null;
        try {
            is.read(buf, -1, 0);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        } 
        buf = new byte[1000];        
        try {
            is.read(buf, -1, 0);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            is.read(buf, 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, -1, -1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, 0, 1001);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, 1001, 0);
            fail("Test 6: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        try {
            is.read(buf, 500, 501);
            fail("Test 7: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        } 
        is.close();
        try {
            is.read(buf, 0, 100);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies reset() in combination with mark().",
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies reset() in combination with mark().",
            method = "mark",
            args = { int.class }
        )
    })    
    public void test_reset() throws Exception {
        try {
            is.reset();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        is = new MyFilterInputStream(new BufferedInputStream(
                new java.io.FileInputStream(fileName), 100));
        try {
            is.reset();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
        }
        final int bufSize = 10;
        byte[] buf1 = new byte[bufSize];
        byte[] buf2 = new byte[bufSize];
        is.read(buf1, 0, bufSize);
        is.mark(2 * bufSize);
        is.read(buf1, 0, bufSize);
        try {
            is.reset();
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException.");
        }
        is.read(buf2, 0, bufSize);
        assertTrue("Test 4: mark() or reset() has failed.", 
                Arrays.equals(buf1, buf2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "skip",
        args = {long.class}
    )    
    public void test_skipJ() throws IOException {
        byte[] buf1 = new byte[10];
        is.skip(10);
        is.read(buf1, 0, buf1.length);
        assertTrue("Test 1: Failed to skip to the correct position.", 
                new String(buf1, 0, buf1.length).equals(
                        testString.substring(10, 20)));
        is.close();
        try {
            is.read();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
        }
    }
    protected void setUp() {
        try {
            fileName = System.getProperty("java.io.tmpdir");
            String separator = System.getProperty("file.separator");
            if (fileName.charAt(fileName.length() - 1) == separator.charAt(0))
                fileName = Support_PlatformFile.getNewPlatformFile(fileName,
                        "input.tst");
            else
                fileName = Support_PlatformFile.getNewPlatformFile(fileName
                        + separator, "input.tst");
            java.io.OutputStream fos = new java.io.FileOutputStream(fileName);
            fos.write(testString.getBytes());
            fos.close();
            is = new MyFilterInputStream(new java.io.FileInputStream(fileName));
        } catch (java.io.IOException e) {
            System.out.println("Exception during setup");
            e.printStackTrace();
        }
    }
    protected void tearDown() {
        try {
            is.close();
        } catch (Exception e) {
            System.out.println("Unexpected exception in tearDown().");
        }
        new java.io.File(fileName).delete();
    }
}
