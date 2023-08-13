@TestTargetClass(PrivilegedActionException.class)
public class PrivilegedActionExceptionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PrivilegedActionExceptionTest.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PrivilegedActionException",
        args = {java.lang.Exception.class}
    )
    public void testPrivilegedActionException() {
        new PrivilegedActionException(null);
        Exception ex = new Exception();
        new PrivilegedActionException(ex);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getException",
        args = {}
    )
    public void testGetException() {
        assertNull(new PrivilegedActionException(null).getException());
        Exception ex = new Exception();
        assertSame(new PrivilegedActionException(ex).getException(), ex);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(new PrivilegedActionException(null).toString());
        assertNotNull(new PrivilegedActionException(new Exception()).toString());
    }
}