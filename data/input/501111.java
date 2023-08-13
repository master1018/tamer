@TestTargetClass(PrivilegedActionException.class)
public class PrivilegedActionException2Test extends junit.framework.TestCase {
    private static Throwable tCause = new Throwable("Test cause");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrivilegedActionException",
        args = {java.lang.Exception.class}
    )
    public void test_ConstructorLjava_lang_Exception() {
        Exception e = new Exception("test exception");
        PrivilegedActionException pe = new PrivilegedActionException(e);
        assertEquals("Did not encapsulate test exception!", e, pe
                .getException());
        pe = new PrivilegedActionException(null);
        assertNull("Did not encapsulate null test exception properly!", pe
                .getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getException",
        args = {}
    )
    public void test_getException() {
        Exception e = new IOException("test IOException");
        PrivilegedActionException pe = new PrivilegedActionException(e);
        assertEquals("Did not encapsulate test IOException!", e, pe
                .getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() {
        Exception ex = new Exception("Test message", tCause);
        PrivilegedActionException pe = new PrivilegedActionException(ex);
        try {
            Throwable res = pe.getCause();
            if (!res.equals(ex)) {
                fail("Method getCause() returned incorrect value");
            }
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
}