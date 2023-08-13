@TestTargetClass(CertificateException.class)
public class CertificateExceptionTest extends TestCase {
    static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertificateException",
        args = {}
    )
    public void testCertificateException01() {
        CertificateException tE = new CertificateException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameter.",
        method = "CertificateException",
        args = {java.lang.String.class}
    )
    public void testCertificateException02() {
        CertificateException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertificateException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CertificateException",
        args = {java.lang.String.class}
    )
    public void testCertificateException03() {
        String msg = null;
        CertificateException tE = new CertificateException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
}
