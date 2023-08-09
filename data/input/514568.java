@TestTargetClass(ErrorManager.class)
public class ErrorManagerTest extends TestCase {
    private final PrintStream err = System.err;
    private final PrintStream out = System.out;
    private OutputStream errSubstituteStream = null; 
    public void setUp() throws Exception{
        super.setUp();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));           
    }
    public void tearDown() throws Exception{
        System.setErr(err);
        System.setOut(out);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case only with ErrorManager.GENERIC_FAILURE, impove MockStream",
        method = "error",
        args = {java.lang.String.class, java.lang.Exception.class, int.class}
    )
    public void test_errorCheck() {
        ErrorManager em = new ErrorManager();
        MockStream aos = new MockStream();
        PrintStream st = new PrintStream(aos);
        System.setErr(st);
        System.setOut(st);
        em.error("supertest", null, ErrorManager.GENERIC_FAILURE);
        st.flush();
       assertTrue("message appears (supertest)", aos.getWrittenData().indexOf("supertest") != -1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case only with ErrorManager.GENERIC_FAILURE",
        method = "error",
        args = {java.lang.String.class, java.lang.Exception.class, int.class}
    )
    public void test_errorStringStringint() {
        ErrorManager em = new ErrorManager();
        em.error(null, new NullPointerException(),
                        ErrorManager.GENERIC_FAILURE);
        em.error("An error message.", null, ErrorManager.GENERIC_FAILURE);
        em.error(null, null, ErrorManager.GENERIC_FAILURE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ErrorManager",
        args = {}
    )
    public void test_constants() {
        assertEquals(3, ErrorManager.CLOSE_FAILURE);
        assertEquals(2, ErrorManager.FLUSH_FAILURE);
        assertEquals(5, ErrorManager.FORMAT_FAILURE);
        assertEquals(0, ErrorManager.GENERIC_FAILURE);
        assertEquals(4, ErrorManager.OPEN_FAILURE);
        assertEquals(1, ErrorManager.WRITE_FAILURE);
    }
    public class MockStream extends ByteArrayOutputStream {
        private StringBuffer linesWritten = new StringBuffer();
        public void flush() {}
        public  void close() {}
        @Override
        public void write(byte[] buffer) {
            linesWritten.append(new String(buffer));
        }
        @Override
        public synchronized void write(byte[] buffer, int offset, int len) {
            linesWritten.append(new String(buffer, offset, len));
        }
        public String getWrittenData() {return linesWritten.toString();}
    }
}
