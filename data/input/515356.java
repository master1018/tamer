@TestTargetClass(UnrecoverableKeyException.class)
public class UnrecoverableKeyExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnrecoverableKeyException",
        args = {}
    )
    public void testUnrecoverableKeyException01() {
        UnrecoverableKeyException tE = new UnrecoverableKeyException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnrecoverableKeyException",
        args = {java.lang.String.class}
    )
    public void testUnrecoverableKeyException02() {
        UnrecoverableKeyException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new UnrecoverableKeyException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "UnrecoverableKeyException",
        args = {java.lang.String.class}
    )
    public void testUnrecoverableKeyException03() {
        String msg = null;
        UnrecoverableKeyException tE = new UnrecoverableKeyException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}
