@TestTargetClass(SSLKeyException.class) 
public class SSLKeyExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLKeyException",
        args = {java.lang.String.class}
    )
    public void test_Constructor01() {
        SSLKeyException skE;
        for (int i = 0; i < msgs.length; i++) {
            skE = new SSLKeyException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), skE.getMessage(), msgs[i]);
            assertNull("getCause() must return null", skE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLKeyException",
        args = {java.lang.String.class}
    )
    public void test_Constructor02() {
        String msg = null;
        SSLKeyException skE = new SSLKeyException(msg);
        assertNull("getMessage() must return null.", skE.getMessage());
        assertNull("getCause() must return null", skE.getCause());
    }
}