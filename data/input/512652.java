@TestTargetClass(NoSuchProviderException.class)
public class NoSuchProviderExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchProviderException",
        args = {}
    )
    public void testNoSuchProviderException01() {
        NoSuchProviderException tE = new NoSuchProviderException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchProviderException",
        args = {java.lang.String.class}
    )
    public void testNoSuchProviderException02() {
        NoSuchProviderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new NoSuchProviderException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "NoSuchProviderException",
        args = {java.lang.String.class}
    )
    public void testNoSuchProviderException03() {
        String msg = null;
        NoSuchProviderException tE = new NoSuchProviderException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}
