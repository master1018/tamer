@TestTargetClass(LoginException.class) 
public class LoginExceptionTest extends TestCase {
    public static void main(String[] args) {
    }
    public LoginExceptionTest(String arg0) {
        super(arg0);
    }
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LoginException",
        args = {}
    )
    public void testLoginException01() {
        LoginException lE = new LoginException();
        assertNull("getMessage() must return null.", lE.getMessage());
        assertNull("getCause() must return null", lE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "LoginException",
        args = {String.class}
    )
    public void testLoginException02() {
        LoginException lE;
        for (int i = 0; i < msgs.length; i++) {
            lE = new LoginException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), lE.getMessage(), msgs[i]);
            assertNull("getCause() must return null", lE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "LoginException",
        args = {String.class}
    )
    public void testLoginException03() {
        String msg = null;
        LoginException lE = new LoginException(msg);
        assertNull("getMessage() must return null.", lE.getMessage());
        assertNull("getCause() must return null", lE.getCause());
    }
}
