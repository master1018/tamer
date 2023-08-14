@TestTargetClass(PipedOutputStream.class) 
public class PipedOutputStreamTest extends junit.framework.TestCase {
    static class PReader implements Runnable {
        PipedInputStream reader;
        public PipedInputStream getReader() {
            return reader;
        }
        public PReader(PipedOutputStream out) {
            try {
                reader = new PipedInputStream(out);
            } catch (Exception e) {
                System.out.println("Exception setting up reader: "
                        + e.toString());
            }
        }
        public int available() {
            try {
                return reader.available();
            } catch (Exception e) {
                return -1;
            }
        }
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                    Thread.yield();
                }
            } catch (InterruptedException e) {
            }
        }
        public String read(int nbytes) {
            byte[] buf = new byte[nbytes];
            try {
                reader.read(buf, 0, nbytes);
                return new String(buf);
            } catch (IOException e) {
                System.out.println("Exception reading ("
                        + Thread.currentThread().getName() + "): "
                        + e.toString());
                return "ERROR";
            }
        }
    }
    static final String testString = "Lorem ipsum dolor sit amet,\n" +
        "consectetur adipisicing elit,\nsed do eiusmod tempor incididunt ut" +
        "labore et dolore magna aliqua.\n";
    static final int testLength = testString.length();
    Thread rt;
    PReader reader;
    PipedOutputStream out;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PipedOutputStream",
        args = {}
    )
    public void test_Constructor() {
        out = new PipedOutputStream();
        assertNotNull(out);
        try {
            out.close();
        } catch (IOException e) {
            fail("Unexpeceted IOException.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PipedOutputStream",
        args = {java.io.PipedInputStream.class}
    )
    public void test_ConstructorLjava_io_PipedInputStream() throws IOException {
        try {
            out = new PipedOutputStream(new PipedInputStream());
            out.write('b');
        } catch (Exception e) {
            fail("Test 1: Constructor failed: " + e.getMessage());
        }
        out.close();
        PipedInputStream pis = new PipedInputStream(new PipedOutputStream());
        try {
            out = new PipedOutputStream(pis);
            fail("Test 2: IOException expected because the input stream is already connected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    public void test_close() {
        out = new PipedOutputStream();
        rt = new Thread(reader = new PReader(out));
        rt.start();
        try {
            out.close();
        } catch (IOException e) {
            fail("Test 1: Unexpected IOException: " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "connect",
        args = {java.io.PipedInputStream.class}
    )
    public void test_connectLjava_io_PipedInputStream() throws IOException {
        out = new PipedOutputStream();
        try {
            out.connect(new PipedInputStream());
        } catch (Exception e) {
            fail("Test 1: Unexpected exception when connecting: " + 
                    e.getLocalizedMessage());
        }
        try {
            out.write('B');
        } catch (IOException e) {
            fail("Test 2: Unexpected IOException when writing after connecting.");
        }
        try {
            out.connect(new PipedInputStream());
            fail("Test 3: IOException expected when reconnecting the stream.");
        } catch (IOException e) {
        }
        try {
            out.connect(null);
            fail("Test 4: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No IOException checking because it is never thrown in the source code.",
        method = "flush",
        args = {}
    )
    public void test_flush() throws Exception {
        out = new PipedOutputStream();
        rt = new Thread(reader = new PReader(out));
        rt.start();
        out.write(testString.getBytes(), 0, 10);
        assertTrue("Test 1: Bytes have been written before flush.", reader.available() != 0);
        out.flush();
        assertEquals("Test 2: Flush failed. ", 
                testString.substring(0, 10), reader.read(10));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test 6 disabled due to incomplete implementation, see ticket #92.",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII() throws IOException {
        out = new PipedOutputStream();
        try {
            out.write(testString.getBytes(), 0, 5);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        out = new PipedOutputStream(new PipedInputStream());
        try {
            out.write(testString.getBytes(), -1, 10);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            out.write(testString.getBytes(), 0, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            out.write(testString.getBytes(), 5, testString.length());
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
        }
        out.close();
        out = new PipedOutputStream();
        try {
            rt = new Thread(reader = new PReader(out));
            rt.start();
            out.write(testString.getBytes(), 0, testString.length());
            out.flush();
            assertEquals("Test 5: Bytes read do not match the bytes written. ", 
                         testString, reader.read(testString.length()));
        } catch (IOException e) {
            fail("Test 5: Unexpected IOException: " + e.getMessage());
        }
        reader.getReader().close();
        try {
            out.write(testString.getBytes(), 0, 5);
            fail("Test 7: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test 3 disabled due to incomplete implementation, see ticket #92.",
        method = "write",
        args = {int.class}
    )
    public void test_writeI() throws IOException {
        out = new PipedOutputStream();
        try {
            out.write(42);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
        rt = new Thread(reader = new PReader(out));
        rt.start();
        out.write('c');
        out.flush();
        assertEquals("Test 2: The byte read does not match the byte written. ", 
                     "c", reader.read(1));
        reader.getReader().close();
        try {
            out.write(42);
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
        }
    }
    protected void tearDown() throws Exception {
        if (rt != null)
            rt.interrupt();
        super.tearDown();
    }
}
