@TestTargetClass(SSLProtocolException.class) 
public class SSLProtocolExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLProtocolException",
        args = {java.lang.String.class}
    )
    public void test_Constructor01() {
        SSLProtocolException sslE;
        for (int i = 0; i < msgs.length; i++) {
            sslE = new SSLProtocolException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), sslE.getMessage(), msgs[i]);
            assertNull("getCause() must return null", sslE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "SSLProtocolException",
        args = {java.lang.String.class}
    )
    public void test_Constructor02() {
        String msg = null;
        SSLProtocolException sslE = new SSLProtocolException(msg);
        assertNull("getMessage() must return null.", sslE.getMessage());
        assertNull("getCause() must return null", sslE.getCause());
    }
}