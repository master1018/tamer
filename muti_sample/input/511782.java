@TestTargetClass(NoSuchPaddingException.class)
public class NoSuchPaddingExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchPaddingException",
        args = {}
    )
    public void testNoSuchPaddingException01() {
        NoSuchPaddingException tE = new NoSuchPaddingException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchPaddingException",
        args = {java.lang.String.class}
    )
    public void testNoSuchPaddingException02() {
        NoSuchPaddingException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchPaddingException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchPaddingException",
        args = {java.lang.String.class}
    )
    public void testNoSuchPaddingException03() {
        String msg = null;
        NoSuchPaddingException tE = new NoSuchPaddingException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}
